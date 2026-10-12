package com.divyansh.aitourplanner.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tools exposed to the AI via Spring AI tool calling. Every tool here is a
 * deterministic, side-effect-free calculation backed by existing backend
 * logic (MapLinkService) - the model never gets direct database or filesystem
 * access. Used by the itinerary-modification and travel-assistant flows.
 */
@Component
public class ItineraryTools {

    private final MapLinkService mapLinkService;

    public ItineraryTools(MapLinkService mapLinkService) {
        this.mapLinkService = mapLinkService;
    }

    @Tool(description = "Calculate the straight-line (great-circle) distance in kilometers between two geographic coordinates. "
            + "This is NOT road distance. For actual travel distance, use a routing service.")
    public double calculateDistanceKm(
            @ToolParam(description = "Latitude of point A") double lat1,
            @ToolParam(description = "Longitude of point A") double lng1,
            @ToolParam(description = "Latitude of point B") double lat2,
            @ToolParam(description = "Longitude of point B") double lng2) {
        return Math.round(mapLinkService.distanceKm(lat1, lng1, lat2, lng2) * 100.0) / 100.0;
    }

    @Tool(description = "Estimate travel time in minutes between two coordinates for a given mode "
            + "of transport (walking, driving, transit, cycling). "
            + "Returns a labeled string making clear this is a straight-line approximation, "
            + "not an actual routed travel time.")
    public String estimateTravelTimeMinutes(
            @ToolParam(description = "Latitude of point A") double lat1,
            @ToolParam(description = "Longitude of point A") double lng1,
            @ToolParam(description = "Latitude of point B") double lat2,
            @ToolParam(description = "Longitude of point B") double lng2,
            @ToolParam(description = "Mode of transport: walking, driving, transit, or cycling") String mode) {
        double km = mapLinkService.distanceKm(lat1, lng1, lat2, lng2);
        return mapLinkService.describeTravelTimeEstimate(km, mode);
    }

    @Tool(description = "Generate a Google Maps navigation URL for a destination coordinate. "
            + "Always use this instead of inventing a maps URL yourself.")
    public String generateMapsLink(
            @ToolParam(description = "Destination latitude") double lat,
            @ToolParam(description = "Destination longitude") double lng) {
        String url = mapLinkService.directionsUrl(lat, lng);
        return url != null ? url : "Invalid coordinates - cannot generate a maps link";
    }

    @Tool(description = "Check whether a geographic coordinate pair is valid/plausible "
            + "(latitude -90..90, longitude -180..180, not 0,0)")
    public boolean isValidCoordinate(
            @ToolParam(description = "Latitude") double lat,
            @ToolParam(description = "Longitude") double lng) {
        return mapLinkService.isValidCoordinate(lat, lng);
    }
}
