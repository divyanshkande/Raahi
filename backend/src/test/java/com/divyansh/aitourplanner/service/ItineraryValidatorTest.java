package com.divyansh.aitourplanner.service;

import com.divyansh.aitourplanner.exception.custom.ItineraryValidationException;
import com.divyansh.aitourplanner.model.AiItineraryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

class ItineraryValidatorTest {

    private ItineraryValidator validator;
    private MapLinkService mapLinkService;

    @BeforeEach
    void setUp() {
        mapLinkService = new MapLinkService();
        validator = new ItineraryValidator(mapLinkService);
    }

    // ===== Valid itinerary tests =====

    @Test
    void validItinerary_passesValidation() {
        AiItineraryResult result = createValidResult(3);

        AiItineraryResult validated = validator.validateAndClean(result, 3);

        assertThat(validated).isSameAs(result);
        assertThat(validated.getItinerary()).hasSize(3);
    }

    @Test
    void validItineraryWithExtraDays_removesExtraDays() {
        AiItineraryResult result = createValidResult(3);
        // Add an extra day
        Map<String, List<AiItineraryResult.AiPlace>> extraDay = new HashMap<>();
        extraDay.put("morning", new ArrayList<>(List.of(createPlace("Extra", 1.0, 1.0))));
        result.getItinerary().put("Day 4", extraDay);

        AiItineraryResult validated = validator.validateAndClean(result, 3);

        assertThat(validated.getItinerary()).hasSize(3);
        assertThat(validated.getItinerary()).doesNotContainKey("Day 4");
    }

    @Test
    void validItineraryWithMissingDays_addsPlaceholders() {
        // This is handled by fillMissingSlots in TourPlanService, not validator
        AiItineraryResult result = createValidResult(3);
        result.getItinerary().remove("Day 2");

        AiItineraryResult validated = validator.validateAndClean(result, 3);

        // Validator only removes invalid day keys, doesn't add missing ones
        assertThat(validated.getItinerary()).hasSize(2);
    }

    // ===== Invalid structure tests =====

    @Test
    void nullResult_throwsException() {
        assertThatThrownBy(() -> validator.validateAndClean(null, 3))
                .isInstanceOf(ItineraryValidationException.class)
                .hasMessageContaining("no itinerary structure");
    }

    @Test
    void emptyItinerary_throwsException() {
        AiItineraryResult result = new AiItineraryResult();
        result.setItinerary(new HashMap<>());

        assertThatThrownBy(() -> validator.validateAndClean(result, 3))
                .isInstanceOf(ItineraryValidationException.class)
                .hasMessageContaining("no itinerary structure");
    }

    @Test
    void invalidDayKey_removed() {
        AiItineraryResult result = createValidResult(3);
        Map<String, List<AiItineraryResult.AiPlace>> invalidDay = new HashMap<>();
        invalidDay.put("morning", new ArrayList<>(List.of(createPlace("Test", 48.8566, 2.3522))));
        result.getItinerary().put("Invalid Day", invalidDay);

        AiItineraryResult validated = validator.validateAndClean(result, 3);

        assertThat(validated.getItinerary()).doesNotContainKey("Invalid Day");
    }

    @Test
    void dayKeyOutOfRange_removed() {
        AiItineraryResult result = createValidResult(3);
        Map<String, List<AiItineraryResult.AiPlace>> day5 = new HashMap<>();
        day5.put("morning", new ArrayList<>(List.of(createPlace("Test", 48.8566, 2.3522))));
        result.getItinerary().put("Day 5", day5);

        AiItineraryResult validated = validator.validateAndClean(result, 3);

        assertThat(validated.getItinerary()).doesNotContainKey("Day 5");
    }

    // ===== Invalid place tests =====

    @Test
    void blankPlaceName_removed() {
        AiItineraryResult result = createValidResult(3);
        List<AiItineraryResult.AiPlace> places = new ArrayList<>(result.getItinerary().get("Day 1").get("morning"));
        places.add(createPlace("", 48.8566, 2.3522)); // blank name
        places.add(createPlace("   ", 48.8566, 2.3522)); // whitespace only
        result.getItinerary().get("Day 1").put("morning", places);

        AiItineraryResult validated = validator.validateAndClean(result, 3);

        List<AiItineraryResult.AiPlace> morning = validated.getItinerary().get("Day 1").get("morning");
        assertThat(morning).hasSize(1); // Only the original valid place
        assertThat(morning.get(0).getName()).isEqualTo("Morning Place 1");
    }

    @Test
    void invalidCoordinates_removed() {
        AiItineraryResult result = createValidResult(3);
        List<AiItineraryResult.AiPlace> places = new ArrayList<>(result.getItinerary().get("Day 1").get("morning"));
        places.add(createPlace("Null Island", 0.0, 0.0));
        places.add(createPlace("Invalid Lat", 91.0, 0.0));
        places.add(createPlace("Invalid Lng", 0.0, 181.0));
        places.add(createPlace("NaN", Double.NaN, 0.0));
        result.getItinerary().get("Day 1").put("morning", places);

        AiItineraryResult validated = validator.validateAndClean(result, 3);

        List<AiItineraryResult.AiPlace> morning = validated.getItinerary().get("Day 1").get("morning");
        assertThat(morning).hasSize(1);
        assertThat(morning.get(0).getName()).isEqualTo("Morning Place 1");
    }

    // ===== Collection limit tests =====

    @Test
    void excessivePlacesPerSlot_truncated() {
        AiItineraryResult result = createValidResult(3);
        List<AiItineraryResult.AiPlace> places = new ArrayList<>(result.getItinerary().get("Day 1").get("morning"));

        // Add many places to exceed MAX_PLACES_PER_SLOT (10)
        for (int i = 0; i < 15; i++) {
            places.add(createPlace("Place " + i, 48.8566 + i * 0.01, 2.3522 + i * 0.01));
        }
        result.getItinerary().get("Day 1").put("morning", places);

        AiItineraryResult validated = validator.validateAndClean(result, 3);

        List<AiItineraryResult.AiPlace> morning = validated.getItinerary().get("Day 1").get("morning");
        assertThat(morning).hasSizeLessThanOrEqualTo(10);
    }

    @Test
    void excessiveTotalPlacesPerDay_truncated() {
        AiItineraryResult result = createValidResult(3);
        Map<String, List<AiItineraryResult.AiPlace>> day1 = result.getItinerary().get("Day 1");

        // Create mutable lists
        Map<String, List<AiItineraryResult.AiPlace>> mutableDay1 = new HashMap<>();
        for (Map.Entry<String, List<AiItineraryResult.AiPlace>> entry : day1.entrySet()) {
            mutableDay1.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        result.getItinerary().put("Day 1", mutableDay1);

        // Add many places across all slots to exceed MAX_TOTAL_PLACES_PER_DAY (30)
        List<AiItineraryResult.AiPlace> morningPlaces = mutableDay1.get("morning");
        for (int i = 0; i < 40; i++) {
            morningPlaces.add(createPlace("Morning " + i, 48.8566 + i * 0.001, 2.3522 + i * 0.001));
        }

        AiItineraryResult validated = validator.validateAndClean(result, 3);

        int total = validated.getItinerary().get("Day 1").values().stream()
                .mapToInt(List::size).sum();
        assertThat(total).isLessThanOrEqualTo(30);
    }

    @Test
    void excessiveRestaurantSuggestions_truncated() {
        AiItineraryResult result = createValidResult(3);
        Map<String, List<String>> restaurants = new HashMap<>();
        List<String> day1Restaurants = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            day1Restaurants.add("Restaurant " + i);
        }
        restaurants.put("Day 1", day1Restaurants);
        result.setRestaurantSuggestions(restaurants);

        AiItineraryResult validated = validator.validateAndClean(result, 3);

        assertThat(validated.getRestaurantSuggestions().get("Day 1")).hasSizeLessThanOrEqualTo(20);
    }

    @Test
    void blankItemsInSuggestions_removed() {
        AiItineraryResult result = createValidResult(3);
        Map<String, List<String>> restaurants = new HashMap<>();
        List<String> day1 = new ArrayList<>(Arrays.asList("Good Restaurant", "", "   ", "Another Good"));
        restaurants.put("Day 1", day1);
        result.setRestaurantSuggestions(restaurants);

        AiItineraryResult validated = validator.validateAndClean(result, 3);

        assertThat(validated.getRestaurantSuggestions().get("Day 1"))
                .containsExactly("Good Restaurant", "Another Good");
    }

    @Test
    void excessiveTravelTips_truncated() {
        AiItineraryResult result = createValidResult(3);
        List<String> tips = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            tips.add("Tip " + i);
        }
        result.setTravelTips(tips);

        AiItineraryResult validated = validator.validateAndClean(result, 3);

        assertThat(validated.getTravelTips()).hasSizeLessThanOrEqualTo(50);
    }

    @Test
    void allPlacesInvalid_throwsException() {
        AiItineraryResult result = new AiItineraryResult();
        Map<String, Map<String, List<AiItineraryResult.AiPlace>>> itinerary = new HashMap<>();

        Map<String, List<AiItineraryResult.AiPlace>> day1 = new HashMap<>();
        day1.put("morning", new ArrayList<>(List.of(createPlace("Bad", 0.0, 0.0))));
        itinerary.put("Day 1", day1);

        result.setItinerary(itinerary);

        assertThatThrownBy(() -> validator.validateAndClean(result, 3))
                .isInstanceOf(ItineraryValidationException.class)
                .hasMessageContaining("no valid places");
    }

    // ===== Helper methods =====

    private AiItineraryResult createValidResult(int days) {
        AiItineraryResult result = new AiItineraryResult();
        Map<String, Map<String, List<AiItineraryResult.AiPlace>>> itinerary = new HashMap<>();

        for (int d = 1; d <= days; d++) {
            Map<String, List<AiItineraryResult.AiPlace>> day = new HashMap<>();
            day.put("morning", new ArrayList<>(List.of(createPlace("Morning Place " + d, 48.8566, 2.3522))));
            day.put("afternoon", new ArrayList<>(List.of(createPlace("Afternoon Place " + d, 48.8566, 2.3522))));
            day.put("evening", new ArrayList<>(List.of(createPlace("Evening Place " + d, 48.8566, 2.3522))));
            itinerary.put("Day " + d, day);
        }

        result.setItinerary(itinerary);
        result.setRestaurantSuggestions(new HashMap<>());
        result.setTransportSuggestions(new HashMap<>());
        result.setTravelTips(new ArrayList<>(List.of("Tip 1", "Tip 2")));
        result.setSafetyTips(new ArrayList<>(List.of("Safety 1")));

        return result;
    }

    private AiItineraryResult.AiPlace createPlace(String name, double lat, double lng) {
        AiItineraryResult.AiPlace place = new AiItineraryResult.AiPlace();
        place.setName(name);
        place.setDescription("Description for " + name);
        place.setLat(lat);
        place.setLng(lng);
        return place;
    }
}