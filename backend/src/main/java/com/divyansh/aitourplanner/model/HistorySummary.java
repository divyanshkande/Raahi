package com.divyansh.aitourplanner.model;

import java.time.LocalDateTime;
import java.util.List;

public class HistorySummary {

    private Long id;
    private String city;
    private int days;
    private List<String> interests;
    private LocalDateTime createdAt;

    public HistorySummary() {
    }

    public HistorySummary(Long id, String city, int days, List<String> interests, LocalDateTime createdAt) {
        this.id = id;
        this.city = city;
        this.days = days;
        this.interests = interests;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public int getDays() {
        return days;
    }

    public List<String> getInterests() {
        return interests;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
