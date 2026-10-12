package com.divyansh.aitourplanner;

import com.divyansh.aitourplanner.exception.custom.ResourceNotFoundException;
import com.divyansh.aitourplanner.model.HistoryDetail;
import com.divyansh.aitourplanner.model.HistorySummary;
import com.divyansh.aitourplanner.model.PageResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/history")
public class SearchHistoryController {

    private static final Logger log = LoggerFactory.getLogger(SearchHistoryController.class);

    private final SearchHistoryService historyService;
    private final UserRepository userRepository;

    public SearchHistoryController(SearchHistoryService historyService, UserRepository userRepository) {
        this.historyService = historyService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<PageResponse<HistorySummary>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {

        User user = getAuthenticatedUser(authentication);
        PageResponse<HistorySummary> history = historyService.listForUser(user, page, size);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoryDetail> getDetail(@PathVariable Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        HistoryDetail detail = historyService.getDetail(id, user);
        return ResponseEntity.ok(detail);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        boolean deleted = historyService.delete(id, user);

        if (!deleted) {
            throw new ResourceNotFoundException("History entry not found.");
        }
        return ResponseEntity.noContent().build();
    }

    private User getAuthenticatedUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));
    }
}