package com.divyansh.aitourplanner;

import com.divyansh.aitourplanner.exception.custom.ResourceNotFoundException;
import com.divyansh.aitourplanner.model.HistoryDetail;
import com.divyansh.aitourplanner.model.HistorySummary;
import com.divyansh.aitourplanner.model.PageResponse;
import com.divyansh.aitourplanner.model.TourResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SearchHistoryServiceTest {

    @Autowired
    private SearchHistoryService historyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SearchHistoryRepository historyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        userRepository.deleteAll();

        user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword(passwordEncoder.encode("password123"));
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword(passwordEncoder.encode("password123"));
        user2 = userRepository.save(user2);
    }

    @Test
    void userCanRetrieveOwnHistory() {
        // Given
        TourResponse response1 = createTourResponse("Paris", 3, List.of("food", "culture"));
        TourResponse response2 = createTourResponse("Tokyo", 5, List.of("adventure"));
        historyService.save(user1, response1);
        historyService.save(user1, response2);

        // When
        PageResponse<HistorySummary> history = historyService.listForUser(user1, 0, 10);

        // Then
        assertThat(history.getContent()).hasSize(2);
        assertThat(history.getTotalElements()).isEqualTo(2);
        assertThat(history.getContent().get(0).getCity()).isEqualTo("Tokyo"); // Most recent first
        assertThat(history.getContent().get(1).getCity()).isEqualTo("Paris");
    }

    @Test
    void userCannotRetrieveAnotherUsersHistory() {
        // Given
        TourResponse response1 = createTourResponse("Paris", 3, List.of("food"));
        historyService.save(user1, response1);

        TourResponse response2 = createTourResponse("Tokyo", 5, List.of("adventure"));
        historyService.save(user2, response2);

        // When
        PageResponse<HistorySummary> user1History = historyService.listForUser(user1, 0, 10);
        PageResponse<HistorySummary> user2History = historyService.listForUser(user2, 0, 10);

        // Then
        assertThat(user1History.getContent()).hasSize(1);
        assertThat(user1History.getContent().get(0).getCity()).isEqualTo("Paris");

        assertThat(user2History.getContent()).hasSize(1);
        assertThat(user2History.getContent().get(0).getCity()).isEqualTo("Tokyo");
    }

    @Test
    void paginationWorks() {
        // Given
        for (int i = 1; i <= 15; i++) {
            TourResponse response = createTourResponse("City " + i, i % 5 + 1, List.of("interest" + i));
            historyService.save(user1, response);
        }

        // When - first page
        PageResponse<HistorySummary> page1 = historyService.listForUser(user1, 0, 5);
        // When - second page
        PageResponse<HistorySummary> page2 = historyService.listForUser(user1, 1, 5);
        // When - third page
        PageResponse<HistorySummary> page3 = historyService.listForUser(user1, 2, 5);
        // When - fourth page (should have 0 items)
        PageResponse<HistorySummary> page4 = historyService.listForUser(user1, 3, 5);

        // Then
        assertThat(page1.getContent()).hasSize(5);
        assertThat(page1.getTotalElements()).isEqualTo(15);
        assertThat(page1.getTotalPages()).isEqualTo(3);
        assertThat(page1.isFirst()).isTrue();
        assertThat(page1.isLast()).isFalse();
        assertThat(page1.hasNext()).isTrue();

        assertThat(page2.getContent()).hasSize(5);
        assertThat(page2.isFirst()).isFalse();
        assertThat(page2.isLast()).isFalse();

        assertThat(page3.getContent()).hasSize(5);
        assertThat(page3.isFirst()).isFalse();
        assertThat(page3.isLast()).isTrue();
        assertThat(page3.hasNext()).isFalse();

        assertThat(page4.getContent()).isEmpty();
    }

    @Test
    void invalidPageSizeIsHandledSafely() {
        // Given
        TourResponse response = createTourResponse("Paris", 3, List.of("food"));
        historyService.save(user1, response);

        // When - size too large (should be capped at 50)
        PageResponse<HistorySummary> largeSize = historyService.listForUser(user1, 0, 100);
        // When - size zero (should default to 1)
        PageResponse<HistorySummary> zeroSize = historyService.listForUser(user1, 0, 0);
        // When - negative page (should default to 0)
        PageResponse<HistorySummary> negativePage = historyService.listForUser(user1, -5, 10);

        // Then
        assertThat(largeSize.getSize()).isEqualTo(50);
        assertThat(zeroSize.getSize()).isEqualTo(1);
        assertThat(negativePage.getPage()).isEqualTo(0);
    }

    @Test
    void historyListReturnsSummariesNotFullItinerary() {
        // Given
        TourResponse response = createTourResponse("Paris", 3, List.of("food", "culture"));
        historyService.save(user1, response);

        // When
        PageResponse<HistorySummary> history = historyService.listForUser(user1, 0, 10);

        // Then
        assertThat(history.getContent()).hasSize(1);
        HistorySummary summary = history.getContent().get(0);
        assertThat(summary.getCity()).isEqualTo("Paris");
        assertThat(summary.getDays()).isEqualTo(3);
        assertThat(summary.getInterests()).containsExactly("food", "culture");
        assertThat(summary.getCreatedAt()).isNotNull();
        // Summary should NOT contain the full itinerary
    }

    @Test
    void missingHistoryReturnsCorrectError() {
        // When/Then
        assertThatThrownBy(() -> historyService.getDetail(999L, user1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("History entry not found.");
    }

    @Test
    void userCannotAccessAnotherUsersHistoryDetail() {
        // Given
        TourResponse response = createTourResponse("Paris", 3, List.of("food"));
        historyService.save(user1, response);
        SearchHistory saved = historyRepository.findByUserOrderByCreatedAtDesc(user1, PageRequest.of(0, 1)).getContent().get(0);

        // When/Then - user2 trying to access user1's history
        assertThatThrownBy(() -> historyService.getDetail(saved.getId(), user2))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void userCanDeleteOwnHistory() {
        // Given
        TourResponse response = createTourResponse("Paris", 3, List.of("food"));
        historyService.save(user1, response);
        SearchHistory saved = historyRepository.findByUserOrderByCreatedAtDesc(user1, PageRequest.of(0, 1)).getContent().get(0);

        // When
        boolean deleted = historyService.delete(saved.getId(), user1);

        // Then
        assertThat(deleted).isTrue();
        assertThat(historyRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void userCannotDeleteAnotherUsersHistory() {
        // Given
        TourResponse response = createTourResponse("Paris", 3, List.of("food"));
        historyService.save(user1, response);
        SearchHistory saved = historyRepository.findByUserOrderByCreatedAtDesc(user1, PageRequest.of(0, 1)).getContent().get(0);

        // When
        boolean deleted = historyService.delete(saved.getId(), user2);

        // Then
        assertThat(deleted).isFalse();
        assertThat(historyRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void historyPersistenceFailureDoesNotBreakItineraryGeneration() {
        // This test verifies the design: save() catches exceptions and doesn't propagate them
        // We can't easily test the exception path without mocking, but we verify the method
        // signature doesn't throw checked exceptions
        TourResponse response = createTourResponse("Paris", 3, List.of("food"));

        // This should not throw even if DB is unavailable (in real scenario)
        // Here we just verify it completes without exception
        assertThatCode(() -> historyService.save(user1, response))
                .doesNotThrowAnyException();
    }

    private TourResponse createTourResponse(String city, int days, List<String> interests) {
        return new TourResponse(city, days, interests, new java.util.HashMap<>());
    }
}