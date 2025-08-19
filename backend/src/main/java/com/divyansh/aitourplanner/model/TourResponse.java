package com.divyansh.aitourplanner.model;

import java.util.List;
import java.util.Map;

public class TourResponse {
    private String city;
    private int days;
    private List<String> interests;
    private Map<String, Map<String, Object>> itinerary;

    

	
    public TourResponse(String city, int days, List<String> interests, Map<String, Map<String, Object>> itinerary) {
        this.city = city;
        this.days = days;
        this.interests = interests;
        this.itinerary = itinerary;
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
    
    public Map<String, Map<String, Object>> getItinerary() {
        return itinerary;
    }
}
