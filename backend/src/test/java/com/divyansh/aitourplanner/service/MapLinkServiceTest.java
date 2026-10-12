package com.divyansh.aitourplanner.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MapLinkServiceTest {

    private final MapLinkService mapLinkService = new MapLinkService();

    // ===== isValidCoordinate tests =====

    @Test
    void validCoordinate_returnsTrue() {
        assertThat(mapLinkService.isValidCoordinate(48.8566, 2.3522)).isTrue(); // Paris
        assertThat(mapLinkService.isValidCoordinate(-33.8688, 151.2093)).isTrue(); // Sydney
        assertThat(mapLinkService.isValidCoordinate(0.0, 1.0)).isTrue(); // Near null island but not 0,0
        assertThat(mapLinkService.isValidCoordinate(90.0, 0.0)).isTrue(); // North Pole
        assertThat(mapLinkService.isValidCoordinate(-90.0, 0.0)).isTrue(); // South Pole
        assertThat(mapLinkService.isValidCoordinate(0.0, 180.0)).isTrue(); // Date line
        assertThat(mapLinkService.isValidCoordinate(0.0, -180.0)).isTrue(); // Date line
        // Edge cases: very small valid coordinates
        assertThat(mapLinkService.isValidCoordinate(0.0001, 0.0001)).isTrue();
        assertThat(mapLinkService.isValidCoordinate(-0.0001, -0.0001)).isTrue();
    }

    @Test
    void invalidCoordinate_returnsFalse() {
        assertThat(mapLinkService.isValidCoordinate(0.0, 0.0)).isFalse(); // Null island
        assertThat(mapLinkService.isValidCoordinate(91.0, 0.0)).isFalse(); // Lat > 90
        assertThat(mapLinkService.isValidCoordinate(-91.0, 0.0)).isFalse(); // Lat < -90
        assertThat(mapLinkService.isValidCoordinate(0.0, 181.0)).isFalse(); // Lng > 180
        assertThat(mapLinkService.isValidCoordinate(0.0, -181.0)).isFalse(); // Lng < -180
        assertThat(mapLinkService.isValidCoordinate(Double.NaN, 0.0)).isFalse(); // NaN lat
        assertThat(mapLinkService.isValidCoordinate(0.0, Double.NaN)).isFalse(); // NaN lng
        assertThat(mapLinkService.isValidCoordinate(Double.POSITIVE_INFINITY, 0.0)).isFalse(); // Infinity
        assertThat(mapLinkService.isValidCoordinate(0.0, Double.NEGATIVE_INFINITY)).isFalse(); // -Infinity
        assertThat(mapLinkService.isValidCoordinate(Double.NaN, Double.NaN)).isFalse(); // Both NaN
    }

    // ===== directionsUrl tests =====

    @Test
    void directionsUrl_validCoordinate_returnsGoogleMapsUrl() {
        String url = mapLinkService.directionsUrl(48.8566, 2.3522);
        assertThat(url).isEqualTo("https://www.google.com/maps/dir/?api=1&destination=48.8566%2C2.3522");
    }

    @Test
    void directionsUrl_invalidCoordinate_returnsNull() {
        assertThat(mapLinkService.directionsUrl(0.0, 0.0)).isNull();
        assertThat(mapLinkService.directionsUrl(91.0, 0.0)).isNull();
        assertThat(mapLinkService.directionsUrl(0.0, 181.0)).isNull();
        assertThat(mapLinkService.directionsUrl(Double.NaN, 0.0)).isNull();
        assertThat(mapLinkService.directionsUrl(0.0, Double.NaN)).isNull();
    }

    @Test
    void directionsUrl_usesUrlEncoding() {
        // Coordinates with special characters that need encoding
        String url = mapLinkService.directionsUrl(48.8566, -2.3522);
        assertThat(url).contains("destination=48.8566%2C-2.3522");
    }

    @Test
    void directionsUrl_precision() {
        // High precision coordinates should be preserved
        String url = mapLinkService.directionsUrl(48.856614, 2.3522219);
        assertThat(url).isEqualTo("https://www.google.com/maps/dir/?api=1&destination=48.856614%2C2.3522219");
    }

    // ===== distanceKm tests =====

    @Test
    void distanceKm_samePoint_returnsZero() {
        double dist = mapLinkService.distanceKm(48.8566, 2.3522, 48.8566, 2.3522);
        assertThat(dist).isCloseTo(0.0, within(0.001));
    }

    @Test
    void distanceKm_parisToLondon_returnsApproximateDistance() {
        // Paris to London ~344 km
        double dist = mapLinkService.distanceKm(48.8566, 2.3522, 51.5074, -0.1278);
        assertThat(dist).isBetween(340.0, 350.0);
    }

    @Test
    void distanceKm_parisToNewYork_returnsApproximateDistance() {
        // Paris to NYC ~5836 km
        double dist = mapLinkService.distanceKm(48.8566, 2.3522, 40.7128, -74.0060);
        assertThat(dist).isBetween(5800.0, 5900.0);
    }

    @Test
    void distanceKm_antipodalPoints_returnsHalfCircumference() {
        // Point to its antipode should be ~20015 km (half Earth's circumference)
        // Use valid coordinates near equator
        double dist = mapLinkService.distanceKm(0.0, 0.1, 0.0, 180.0);
        assertThat(dist).isCloseTo(20015.0, within(100.0));
    }

    @Test
    void distanceKm_symmetric() {
        double dist1 = mapLinkService.distanceKm(48.8566, 2.3522, 51.5074, -0.1278);
        double dist2 = mapLinkService.distanceKm(51.5074, -0.1278, 48.8566, 2.3522);
        assertThat(dist1).isEqualTo(dist2);
    }

    @Test
    void distanceKm_invalidCoordinates_returnsNaN() {
        assertThat(mapLinkService.distanceKm(0.0, 0.0, 48.8566, 2.3522)).isNaN();
        assertThat(mapLinkService.distanceKm(48.8566, 2.3522, 91.0, 0.0)).isNaN();
        assertThat(mapLinkService.distanceKm(Double.NaN, 0.0, 48.8566, 2.3522)).isNaN();
        assertThat(mapLinkService.distanceKm(48.8566, 2.3522, Double.NaN, 0.0)).isNaN();
    }

    // ===== estimateTravelTimeMinutes tests =====

    @Test
    void estimateTravelTimeMinutes_zeroDistance_returnsMinimum() {
        int time = mapLinkService.estimateTravelTimeMinutes(0.0, "driving");
        assertThat(time).isEqualTo(1);
    }

    @Test
    void estimateTravelTimeMinutes_negativeDistance_returnsMinimum() {
        int time = mapLinkService.estimateTravelTimeMinutes(-10.0, "driving");
        assertThat(time).isEqualTo(1);
    }

    @Test
    void estimateTravelTimeMinutes_nanDistance_returnsMinimum() {
        int time = mapLinkService.estimateTravelTimeMinutes(Double.NaN, "driving");
        assertThat(time).isEqualTo(1);
    }

    @Test
    void estimateTravelTimeMinutes_walking_returnsReasonableTime() {
        // 5 km walking at 4.5 km/h with 30% padding = 5*1.3/4.5 = 1.44h = 86 min
        int time = mapLinkService.estimateTravelTimeMinutes(5.0, "walking");
        assertThat(time).isBetween(80, 95);
    }

    @Test
    void estimateTravelTimeMinutes_driving_returnsReasonableTime() {
        // 100 km driving at 25 km/h with 30% padding = 100*1.3/25 = 5.2h = 312 min
        int time = mapLinkService.estimateTravelTimeMinutes(100.0, "driving");
        assertThat(time).isBetween(300, 330);
    }

    @Test
    void estimateTravelTimeMinutes_transit_returnsReasonableTime() {
        int time = mapLinkService.estimateTravelTimeMinutes(50.0, "transit");
        assertThat(time).isBetween(170, 200);
    }

    @Test
    void estimateTravelTimeMinutes_cycling_returnsReasonableTime() {
        int time = mapLinkService.estimateTravelTimeMinutes(30.0, "cycling");
        assertThat(time).isBetween(160, 190);
    }

    @Test
    void estimateTravelTimeMinutes_unknownMode_defaultsToDriving() {
        int timeDriving = mapLinkService.estimateTravelTimeMinutes(100.0, "driving");
        int timeUnknown = mapLinkService.estimateTravelTimeMinutes(100.0, "flying");
        int timeNull = mapLinkService.estimateTravelTimeMinutes(100.0, null);
        int timeEmpty = mapLinkService.estimateTravelTimeMinutes(100.0, "");

        assertThat(timeUnknown).isEqualTo(timeDriving);
        assertThat(timeNull).isEqualTo(timeDriving);
        assertThat(timeEmpty).isEqualTo(timeDriving);
    }

    @Test
    void estimateTravelTimeMinutes_caseInsensitive() {
        int walk = mapLinkService.estimateTravelTimeMinutes(10.0, "walking");
        int walkUpper = mapLinkService.estimateTravelTimeMinutes(10.0, "WALKING");
        int walkMixed = mapLinkService.estimateTravelTimeMinutes(10.0, "Walking");
        assertThat(walk).isEqualTo(walkUpper).isEqualTo(walkMixed);
    }

    @Test
    void estimateTravelTimeMinutes_veryShortDistance_returnsMinimum() {
        // 0.1 km should still return at least 1 minute
        int time = mapLinkService.estimateTravelTimeMinutes(0.1, "walking");
        assertThat(time).isGreaterThanOrEqualTo(1);
    }

    @Test
    void estimateTravelTimeMinutes_largeDistance() {
        // 1000 km driving = ~52 hours
        int time = mapLinkService.estimateTravelTimeMinutes(1000.0, "driving");
        assertThat(time).isBetween(3000, 3300);
    }

    @Test
    void directionsUrl_handlesSpecialCharactersInCoordinates() {
        // Negative coordinates and high precision
        String url = mapLinkService.directionsUrl(-33.8688, 151.2093);
        assertThat(url).contains("destination=-33.8688%2C151.2093");
    }

    @Test
    void directionsUrl_invalidCoordinatesReturnsNull() {
        assertThat(mapLinkService.directionsUrl(0.0, 0.0)).isNull();
        assertThat(mapLinkService.directionsUrl(91.0, 0.0)).isNull();
        assertThat(mapLinkService.directionsUrl(0.0, 181.0)).isNull();
        assertThat(mapLinkService.directionsUrl(Double.NaN, 0.0)).isNull();
    }
}