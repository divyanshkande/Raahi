package com.divyansh.aitourplanner.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class TourResponse {

    private String city;
    private int days;
    private List<String> interests;

    private Map<String, Map<String, List<Place>>> itinerary;

    // ---- Additive fields (existing frontend/consumers can ignore these) ----
    // Restaurant suggestions per day, e.g. "Day 1" -> ["Cafe X", "Restaurant Y"]
    private Map<String, List<String>> restaurantSuggestions;
    // Transport suggestions per day, e.g. "Day 1" -> ["Metro", "Auto-rickshaw"]
    private Map<String, List<String>> transportSuggestions;
    // Trip-level (not per-day) general advice.
    private List<String> travelTips;
    private List<String> safetyTips;

    public TourResponse(String city, int days, List<String> interests,
                        Map<String, Map<String, List<Place>>> itinerary) {
        this.city = city;
        this.days = days;
        this.interests = interests;
        this.itinerary = itinerary;
    }

    // @JsonCreator so Jackson can deserialize this class (needed when reading
    // a saved history entry's stored JSON back into an object) - without this,
    // Jackson has no setters to populate city/days/interests/itinerary with.
    @JsonCreator
    public TourResponse(@JsonProperty("city") String city,
                        @JsonProperty("days") int days,
                        @JsonProperty("interests") List<String> interests,
                        @JsonProperty("itinerary") Map<String, Map<String, List<Place>>> itinerary,
                        @JsonProperty("restaurantSuggestions") Map<String, List<String>> restaurantSuggestions,
                        @JsonProperty("transportSuggestions") Map<String, List<String>> transportSuggestions,
                        @JsonProperty("travelTips") List<String> travelTips,
                        @JsonProperty("safetyTips") List<String> safetyTips) {
        this(city, days, interests, itinerary);
        this.restaurantSuggestions = restaurantSuggestions;
        this.transportSuggestions = transportSuggestions;
        this.travelTips = travelTips;
        this.safetyTips = safetyTips;
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

    public Map<String, List<String>> getRestaurantSuggestions() {
        return restaurantSuggestions;
    }

    public void setRestaurantSuggestions(Map<String, List<String>> restaurantSuggestions) {
        this.restaurantSuggestions = restaurantSuggestions;
    }

    public Map<String, List<String>> getTransportSuggestions() {
        return transportSuggestions;
    }

    public void setTransportSuggestions(Map<String, List<String>> transportSuggestions) {
        this.transportSuggestions = transportSuggestions;
    }

    public List<String> getTravelTips() {
        return travelTips;
    }

    public void setTravelTips(List<String> travelTips) {
        this.travelTips = travelTips;
    }

    public List<String> getSafetyTips() {
        return safetyTips;
    }

    public void setSafetyTips(List<String> safetyTips) {
        this.safetyTips = safetyTips;
    }

    // ----------------------------------------
    // INNER CLASS: Place
    // ----------------------------------------
    public static class Place {
        private String name;
        private String description;
        private double lat;
        private double lng;
        // Generated server-side from lat/lng (see MapLinkService) - never
        // trust an AI-generated URL here.
        private String mapsUrl;
        // True if coordinates were verified (not duplicated/suspicious), false if
        // coordinates are likely incorrect (e.g., all stops share city center).
        private boolean coordinatesVerified;

        public Place() {
        }

        @JsonCreator
        public Place(@JsonProperty("name") String name,
                     @JsonProperty("description") String description,
                     @JsonProperty("lat") double lat,
                     @JsonProperty("lng") double lng) {
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

        public String getMapsUrl() {
            return mapsUrl;
        }

        public void setMapsUrl(String mapsUrl) {
            this.mapsUrl = mapsUrl;
        }

        public boolean isCoordinatesVerified() {
            return coordinatesVerified;
        }

        public void setCoordinatesVerified(boolean coordinatesVerified) {
            this.coordinatesVerified = coordinatesVerified;
        }
    }
}