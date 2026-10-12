package com.divyansh.aitourplanner.model;

import java.time.LocalDateTime;

public class HistoryDetail {

    private Long id;
    private LocalDateTime createdAt;
    private TourResponse itinerary;

    public HistoryDetail() {
    }

    public HistoryDetail(Long id, LocalDateTime createdAt, TourResponse itinerary) {
        this.id = id;
        this.createdAt = createdAt;
        this.itinerary = itinerary;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public TourResponse getItinerary() {
        return itinerary;
    }
}
