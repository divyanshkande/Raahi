package com.divyansh.aitourplanner;

import com.divyansh.aitourplanner.exception.custom.ResourceNotFoundException;
import com.divyansh.aitourplanner.model.HistoryDetail;
import com.divyansh.aitourplanner.model.HistorySummary;
import com.divyansh.aitourplanner.model.PageResponse;
import com.divyansh.aitourplanner.model.TourResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class SearchHistoryService {

    private static final Logger log = LoggerFactory.getLogger(SearchHistoryService.class);

    private final SearchHistoryRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SearchHistoryService(SearchHistoryRepository repository) {
        this.repository = repository;
    }

    /**
     * Saves a generated itinerary to the user's history. Failures here are
     * logged and swallowed - history is a convenience feature and must never
     * break the itinerary-generation flow itself.
     */
    @Transactional
    public void save(User user, TourResponse tourResponse) {
        long start = System.nanoTime();
        try {
            SearchHistory entry = new SearchHistory();
            entry.setUser(user);
            entry.setCity(tourResponse.getCity());
            entry.setDays(tourResponse.getDays());
            entry.setInterests(tourResponse.getInterests() == null
                    ? "" : String.join(",", tourResponse.getInterests()));
            entry.setItineraryJson(objectMapper.writeValueAsString(tourResponse));
            entry.setCreatedAt(LocalDateTime.now());
            repository.save(entry);
            log.debug("Search history save took {} ms for userId={}", (System.nanoTime() - start) / 1_000_000, user.getId());
        } catch (Exception ex) {
            log.error("Failed to save search history for userId={}: {}", user.getId(), ex.getMessage());
        }
    }

    public PageResponse<HistorySummary> listForUser(User user, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 50); // Cap at 50
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<SearchHistory> historyPage = repository.findByUserOrderByCreatedAtDesc(user, pageable);

        List<HistorySummary> content = historyPage.getContent().stream()
                .map(this::toSummary)
                .toList();

        return new PageResponse<>(
                content,
                historyPage.getNumber(),
                historyPage.getSize(),
                historyPage.getTotalElements(),
                historyPage.getTotalPages(),
                historyPage.isFirst(),
                historyPage.isLast()
        );
    }

    public HistoryDetail getDetail(Long id, User user) {
        return repository.findByIdAndUser(id, user)
                .map(this::toDetail)
                .orElseThrow(() -> new ResourceNotFoundException("History entry not found."));
    }

    /** Returns true if a matching entry was found and deleted, false if it didn't belong to this user. */
    @Transactional
    public boolean delete(Long id, User user) {
        if (!repository.existsByIdAndUser(id, user)) {
            return false;
        }
        repository.deleteByIdAndUser(id, user);
        return true;
    }

    private HistorySummary toSummary(SearchHistory entry) {
        List<String> interests = (entry.getInterests() == null || entry.getInterests().isBlank())
                ? List.of()
                : Arrays.asList(entry.getInterests().split(","));
        return new HistorySummary(entry.getId(), entry.getCity(), entry.getDays(), interests, entry.getCreatedAt());
    }

    private HistoryDetail toDetail(SearchHistory entry) {
        try {
            TourResponse tourResponse = objectMapper.readValue(entry.getItineraryJson(), TourResponse.class);
            return new HistoryDetail(entry.getId(), entry.getCreatedAt(), tourResponse);
        } catch (Exception ex) {
            log.error("Failed to parse stored itinerary for history id={}: {}", entry.getId(), ex.getMessage());
            throw new IllegalStateException("Could not load this history entry.");
        }
    }
}