package com.divyansh.aitourplanner.model;

import java.util.List;
import java.util.Map;

/**
 * Shape the LLM is asked to return via Spring AI structured output.
 * Intentionally separate from {@link TourResponse} - fields like mapsUrl are
 * never trusted from the AI and are always computed server-side before this
 * gets converted into a TourResponse.
 */
public class AiItineraryResult {

    private Map<String, Map<String, List<AiPlace>>> itinerary;
    private Map<String, List<String>> restaurantSuggestions;
    private Map<String, List<String>> transportSuggestions;
    private List<String> travelTips;
    private List<String> safetyTips;

    public AiItineraryResult() {
    }

    public Map<String, Map<String, List<AiPlace>>> getItinerary() {
        return itinerary;
    }

    public void setItinerary(Map<String, Map<String, List<AiPlace>>> itinerary) {
        this.itinerary = itinerary;
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

    public static class AiPlace {
        private String name;
        private String description;
        private double lat;
        private double lng;
        private boolean coordinatesVerified;

        public AiPlace() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public boolean isCoordinatesVerified() {
            return coordinatesVerified;
        }

        public void setCoordinatesVerified(boolean coordinatesVerified) {
            this.coordinatesVerified = coordinatesVerified;
        }
    }
}
