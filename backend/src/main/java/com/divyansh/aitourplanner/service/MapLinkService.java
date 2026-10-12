package com.divyansh.aitourplanner.service;

import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Generates Google Maps links and validates coordinates in backend code
 * rather than trusting anything the AI produces.
 *
 * <p><strong>Important Limitations:</strong>
 * <ul>
 *   <li>This service only validates coordinate <em>syntax/ranges</em>, not
 *       geographic correctness. Valid coordinates may still point to the wrong location.</li>
 *   <li>Haversine distance is straight-line (great-circle) distance, not road distance.</li>
 *   <li>Travel time estimates are rough approximations based on straight-line distance
 *       with a 30% padding factor. They do NOT account for traffic, road conditions,
 *       transit schedules, or routing constraints.</li>
 *   <li>True place-to-coordinate verification requires an external geocoding service,
 *       which is not implemented in this MVP.</li>
 * </ul>
 */
@Service
public class MapLinkService {

    // Earth radius in kilometers (WGS84 mean radius)
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Validates a coordinate pair.
     *
     * @param lat latitude in degrees
     * @param lng longitude in degrees
     * @return true if the coordinate is syntactically valid and not (0,0)
     */
    public boolean isValidCoordinate(double lat, double lng) {
        if (Double.isNaN(lat) || Double.isNaN(lng)) return false;
        if (Double.isInfinite(lat) || Double.isInfinite(lng)) return false;
        if (lat < -90.0 || lat > 90.0) return false;
        if (lng < -180.0 || lng > 180.0) return false;
        // (0,0) is "null island" - almost always a placeholder/missing value.
        if (lat == 0.0 && lng == 0.0) return false;
        return true;
    }

    /**
     * Generates a Google Maps directions URL for a destination coordinate.
     * Coordinates are URL-encoded for safety.
     *
     * @param lat destination latitude
     * @param lng destination longitude
     * @return Google Maps URL, or null if coordinates are invalid
     */
    public String directionsUrl(double lat, double lng) {
        if (!isValidCoordinate(lat, lng)) return null;
        String encodedLat = URLEncoder.encode(String.valueOf(lat), StandardCharsets.UTF_8);
        String encodedLng = URLEncoder.encode(String.valueOf(lng), StandardCharsets.UTF_8);
        return "https://www.google.com/maps/dir/?api=1&destination=" + encodedLat + "%2C" + encodedLng;
    }

    /**
     * Haversine distance in kilometers between two coordinates.
     * This is the great-circle (straight-line) distance, not road distance.
     *
     * @param lat1 latitude of point A
     * @param lng1 longitude of point A
     * @param lat2 latitude of point B
     * @param lng2 longitude of point B
     * @return distance in kilometers
     */
    public double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        // Guard against invalid inputs
        if (!isValidCoordinate(lat1, lng1) || !isValidCoordinate(lat2, lng2)) {
            return Double.NaN;
        }

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Rough travel time estimate in minutes for a straight-line distance.
     *
     * <p><strong>This is an APPROXIMATE ESTIMATE only.</strong> It is based on straight-line
     * (great-circle) distance with a 30% padding factor and average speeds.
     * It does NOT account for:
     * <ul>
     *   <li>Actual road networks or routing</li>
     *   <li>Traffic conditions</li>
     *   <li>Transit schedules or waiting times</li>
     *   <li>Terrain, elevation, or road conditions</li>
     *   <li>Rest stops, transfers, or parking</li>
     * </ul>
     * <p>This should be labeled as "straight-line estimate" in any user-facing display.
     * A real routing service (Google Maps, OSRM, GraphHopper) should be used for
     * accurate travel times.
     *
     * @param distanceKm straight-line distance in kilometers (from Haversine)
     * @param mode transport mode: walking, driving, transit, cycling (case-insensitive)
     * @return estimated travel time in minutes, minimum 1
     */
    public int estimateTravelTimeMinutes(double distanceKm, String mode) {
        // Handle invalid/negative distances
        if (Double.isNaN(distanceKm) || distanceKm <= 0) {
            return 1; // minimum 1 minute
        }

        double kmPerHour = switch (mode == null ? "" : mode.toLowerCase()) {
            case "walking", "walk", "foot" -> 4.5;
            case "transit", "bus", "metro", "public_transport" -> 22.0;
            case "cycling", "bike", "bicycle" -> 14.0;
            default -> 25.0; // driving / auto-rickshaw / taxi default
        };

        // Real routes are never a straight line - pad by ~30%.
        double hours = (distanceKm * 1.3) / kmPerHour;
        return Math.max(1, (int) Math.round(hours * 60));
    }

    /**
     * Returns a labeled description of the travel time estimate for display purposes.
     * Makes it clear this is a straight-line approximation, not a routed travel time.
     */
    public String describeTravelTimeEstimate(double distanceKm, String mode) {
        if (Double.isNaN(distanceKm) || distanceKm <= 0) {
            return "Distance unknown - travel time unavailable";
        }
        int minutes = estimateTravelTimeMinutes(distanceKm, mode);
        String modeLabel = (mode == null || mode.isBlank()) ? "driving" : mode.toLowerCase();
        return String.format("~%d min %s (straight-line estimate, not routed)", minutes, modeLabel);
    }
}