package com.divyansh.aitourplanner.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public class ModifyItineraryRequest {

    @NotBlank(message = "City is required")
    @Size(min = 1, max = 100, message = "City must be between 1 and 100 characters")
    private String city;

    @NotNull(message = "Days is required")
    @Min(value = 1, message = "Days must be at least 1")
    @Max(value = 30, message = "Days must not exceed 30")
    private Integer days;

    @Size(max = 10, message = "Maximum 10 interests allowed")
    private List<@Size(max = 50, message = "Each interest must not exceed 50 characters") String> interests;

    @NotBlank(message = "Instruction is required")
    @Size(min = 1, max = 500, message = "Instruction must be between 1 and 500 characters")
    private String instruction;

    @NotNull(message = "Current itinerary is required")
    private Map<String, Map<String, List<TourResponse.Place>>> currentItinerary;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public Map<String, Map<String, List<TourResponse.Place>>> getCurrentItinerary() {
        return currentItinerary;
    }

    public void setCurrentItinerary(Map<String, Map<String, List<TourResponse.Place>>> currentItinerary) {
        this.currentItinerary = currentItinerary;
    }
}