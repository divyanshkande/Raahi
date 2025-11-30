package com.divyansh.aitourplanner.model;

import java.util.List;
import java.util.Map;

public class TourResponse {

    private String city;
    private int days;
    private List<String> interests;

    private Map<String, Map<String, List<Place>>> itinerary;

    public TourResponse(String city, int days, List<String> interests,
                        Map<String, Map<String, List<Place>>> itinerary) {
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

    public Map<String, Map<String, List<Place>>> getItinerary() {
        return itinerary;
    }

    // ----------------------------------------
    // INNER CLASS: Place
    // ----------------------------------------
    public static class Place {
        private String name;
        private String description;
        private double lat;
        private double lng;

        public Place() {
        }

        public Place(String name, String description, double lat, double lng) {
            this.name = name;
            this.description = description;
            this.lat = lat;
            this.lng = lng;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }
    }
}

