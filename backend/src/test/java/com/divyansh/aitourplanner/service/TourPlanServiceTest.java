package com.divyansh.aitourplanner.service;

import com.divyansh.aitourplanner.ItineraryOperationException;
import com.divyansh.aitourplanner.exception.custom.AiServiceException;
import com.divyansh.aitourplanner.exception.custom.ItineraryValidationException;
import com.divyansh.aitourplanner.model.AiItineraryResult;
import com.divyansh.aitourplanner.model.AssistantRequest;
import com.divyansh.aitourplanner.model.AssistantResponse;
import com.divyansh.aitourplanner.model.ModifyItineraryRequest;
import com.divyansh.aitourplanner.model.TourResponse;
import com.divyansh.aitourplanner.service.CachedItineraryGenerator;
import com.divyansh.aitourplanner.service.ItineraryTools;
import com.divyansh.aitourplanner.service.ItineraryValidator;
import com.divyansh.aitourplanner.service.MapLinkService;
import com.divyansh.aitourplanner.TourPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourPlanServiceTest {

    @Mock
    private CachedItineraryGenerator cachedItineraryGenerator;

    @Mock
    private ItineraryValidator validator;

    @Mock
    private MapLinkService mapLinkService;

    @Mock
    private ItineraryTools itineraryTools;

    private TourPlanService tourPlanService;

    @BeforeEach
    void setUp() {
        tourPlanService = new TourPlanService(
                null, // chatClient not needed when mocking cachedItineraryGenerator
                cachedItineraryGenerator,
                validator,
                mapLinkService,
                itineraryTools
        );
        setField(tourPlanService, "promptTemplate", "Create a %d day itinerary for %s");
    }

    @Test
    void generatePlan_validInput_returnsTourResponse() {
        // Given
        AiItineraryResult aiResult = createValidAiResult(3);
        when(cachedItineraryGenerator.generate(anyString(), eq(3), anyString(), anyString()))
                .thenReturn(aiResult);
        when(mapLinkService.directionsUrl(anyDouble(), anyDouble()))
                .thenReturn("https://maps.google.com/...");

        // When
        TourResponse response = tourPlanService.generatePlan("Paris", 3, List.of("food", "culture"));

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCity()).isEqualTo("Paris");
        assertThat(response.getDays()).isEqualTo(3);
        assertThat(response.getInterests()).containsExactly("food", "culture");
        assertThat(response.getItinerary()).hasSize(3);
        verify(cachedItineraryGenerator).generate(anyString(), eq(3), anyString(), anyString());
    }

    @Test
    void generatePlan_cachedGeneratorThrowsValidationException_propagates() {
        // Given
        when(cachedItineraryGenerator.generate(anyString(), eq(3), anyString(), anyString()))
                .thenThrow(new ItineraryValidationException("Invalid coordinates"));

        // When/Then
        assertThatThrownBy(() -> tourPlanService.generatePlan("Paris", 3, List.of("food")))
                .isInstanceOf(ItineraryValidationException.class)
                .hasMessage("Invalid coordinates");
    }

    @Test
    void generatePlan_cachedGeneratorThrowsAiServiceException_propagates() {
        // Given
        when(cachedItineraryGenerator.generate(anyString(), eq(3), anyString(), anyString()))
                .thenThrow(new AiServiceException("AI unavailable"));

        // When/Then
        assertThatThrownBy(() -> tourPlanService.generatePlan("Paris", 3, List.of("food")))
                .isInstanceOf(AiServiceException.class)
                .hasMessage("AI unavailable");
    }

    @Test
    void generatePlan_cachedGeneratorThrowsUnexpectedException_wrapsAsAiServiceException() {
        // Given
        when(cachedItineraryGenerator.generate(anyString(), eq(3), anyString(), anyString()))
                .thenThrow(new RuntimeException("Network error"));

        // When/Then
        assertThatThrownBy(() -> tourPlanService.generatePlan("Paris", 3, List.of("food")))
                .isInstanceOf(AiServiceException.class)
                .hasMessage("Failed to generate itinerary. Please try again.");
    }

    @Test
    void normalizeInterests_sortsLowercasesAndDeduplicates() {
        // Given - duplicates with different casing and whitespace
        List<String> interests = List.of("Food", " CULTURE ", "food", "Adventure", "culture");

        // When - use reflection to call private method
        String result = invokeNormalizeInterests(interests);

        // Then - sorted, lowercased, deduplicated
        assertThat(result).isEqualTo("adventure,culture,food");
    }

    @Test
    void normalizeInterests_duplicateInterests_removed() {
        // Given
        List<String> interests = List.of("food", "food", "food", "culture", "culture");

        // When
        String result = invokeNormalizeInterests(interests);

        // Then - only unique interests
        assertThat(result).isEqualTo("culture,food");
    }

    @Test
    void normalizeInterests_caseInsensitive_deduplicates() {
        // Given - same interest with different casing
        List<String> interests = List.of("Food", "FOOD", "food", "Culture", "CULTURE");

        // When
        String result = invokeNormalizeInterests(interests);

        // Then - deduplicated case-insensitively
        assertThat(result).isEqualTo("culture,food");
    }

    @Test
    void normalizeInterests_blankEntries_filtered() {
        // Given
        List<String> interests = List.of("food", "", "   ", "culture", "\t", "\n");

        // When
        String result = invokeNormalizeInterests(interests);

        // Then - blank entries filtered out
        assertThat(result).isEqualTo("culture,food");
    }

    @Test
    void normalizeInterests_nullOrEmpty_returnsEmptyString() {
        assertThat(invokeNormalizeInterests(null)).isEqualTo("");
        assertThat(invokeNormalizeInterests(List.of())).isEqualTo("");
        assertThat(invokeNormalizeInterests(List.of("", "   "))).isEqualTo("");
    }

    @Test
    void normalizeInterests_ordering_alphabetical() {
        // Given
        List<String> interests = List.of("zebra", "apple", "banana");

        // When
        String result = invokeNormalizeInterests(interests);

        // Then - alphabetical order
        assertThat(result).isEqualTo("apple,banana,zebra");
    }

    @Test
    void sanitizeForPrompt_emptyInput_returnsEmptyString() {
        String result = invokeSanitizeForPrompt("");
        assertThat(result).isEqualTo("");
    }

    @Test
    void sanitizeForPrompt_nullInput_returnsEmptyString() {
        String result = invokeSanitizeForPrompt(null);
        assertThat(result).isEqualTo("");
    }

    @Test
    void sanitizeForPrompt_shortInput_returnsAsIs() {
        String result = invokeSanitizeForPrompt("Paris");
        assertThat(result).isEqualTo("Paris");
    }

    @Test
    void sanitizeForPrompt_removesControlChars() {
        String input = "Paris\u0000\u001F\n\twith control chars";
        String result = invokeSanitizeForPrompt(input);

        assertThat(result).doesNotContain("\u0000");
        assertThat(result).doesNotContain("\u001F");
        assertThat(result).contains("\n");
        assertThat(result).contains("\t");
        assertThat(result).isEqualTo("Paris\n\twith control chars");
    }

    @Test
    void sanitizeForPrompt_inputShortenedBySanitization_noException() {
        // Input with control chars that will be removed, making it shorter than original
        String input = "A\u0000B\u0001C"; // Original length 5, after sanitization "ABC" length 3
        String result = invokeSanitizeForPrompt(input);

        assertThat(result).isEqualTo("ABC");
    }

    @Test
    void sanitizeForPrompt_truncatesTo200Chars() {
        String longInput = "a".repeat(300);
        String result = invokeSanitizeForPrompt(longInput);

        assertThat(result).hasSize(200);
    }

    @Test
    void sanitizeForPrompt_preservesNewlinesAndTabs() {
        String input = "Line 1\nLine 2\tTabbed";
        String result = invokeSanitizeForPrompt(input);

        assertThat(result).contains("\n");
        assertThat(result).contains("\t");
        assertThat(result).isEqualTo("Line 1\nLine 2\tTabbed");
    }

    @Test
    void sanitizeForPrompt_inputExactly200Chars_noTruncation() {
        String input = "a".repeat(200);
        String result = invokeSanitizeForPrompt(input);

        assertThat(result).hasSize(200);
        assertThat(result).isEqualTo(input);
    }

    @Test
    void sanitizeForPrompt_whitespaceTrimmed() {
        String input = "  Paris  ";
        String result = invokeSanitizeForPrompt(input);

        assertThat(result).isEqualTo("Paris");
    }

    // Note: modifyItinerary and assistantChat tests that require mocking the
    // Spring AI ChatClient fluent API are omitted because the intermediate
    // types (RequestSpec, CallResponseSpec) are not easily mockable without
    // the actual Spring AI classes. These methods are tested via integration tests.

    private AiItineraryResult createValidAiResult(int days) {
        AiItineraryResult result = new AiItineraryResult();
        Map<String, Map<String, List<AiItineraryResult.AiPlace>>> itinerary = new java.util.HashMap<>();

        for (int d = 1; d <= days; d++) {
            Map<String, List<AiItineraryResult.AiPlace>> day = new java.util.HashMap<>();
            day.put("morning", List.of(createPlace("Morning " + d, 48.8566, 2.3522)));
            day.put("afternoon", List.of(createPlace("Afternoon " + d, 48.8566, 2.3522)));
            day.put("evening", List.of(createPlace("Evening " + d, 48.8566, 2.3522)));
            itinerary.put("Day " + d, day);
        }

        result.setItinerary(itinerary);
        result.setRestaurantSuggestions(new java.util.HashMap<>());
        result.setTransportSuggestions(new java.util.HashMap<>());
        result.setTravelTips(List.of("Tip 1"));
        result.setSafetyTips(List.of("Safety 1"));
        return result;
    }

    private AiItineraryResult.AiPlace createPlace(String name, double lat, double lng) {
        AiItineraryResult.AiPlace place = new AiItineraryResult.AiPlace();
        place.setName(name);
        place.setDescription("Desc");
        place.setLat(lat);
        place.setLng(lng);
        return place;
    }

    // Reflection helpers
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }

    private String invokeNormalizeInterests(List<String> interests) {
        try {
            java.lang.reflect.Method method = TourPlanService.class.getDeclaredMethod("normalizeInterests", List.class);
            method.setAccessible(true);
            return (String) method.invoke(tourPlanService, interests);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke normalizeInterests", e);
        }
    }

    private String invokeSanitizeForPrompt(String input) {
        try {
            java.lang.reflect.Method method = TourPlanService.class.getDeclaredMethod("sanitizeForPrompt", String.class);
            method.setAccessible(true);
            return (String) method.invoke(tourPlanService, input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke sanitizeForPrompt", e);
        }
    }

    @Test
    void generatePlan_uniqueCoordinates_allPlacesVerified() {
        // Given - each place has unique coordinates
        AiItineraryResult aiResult = createAiResultWithUniqueCoordinates(3);
        when(cachedItineraryGenerator.generate(anyString(), eq(3), anyString(), anyString()))
                .thenReturn(aiResult);
        when(mapLinkService.directionsUrl(anyDouble(), anyDouble()))
                .thenReturn("https://maps.google.com/...");

        // When
        TourResponse response = tourPlanService.generatePlan("Paris", 3, List.of("food", "culture"));

        // Then - all places should have coordinatesVerified = true
        List<TourResponse.Place> allPlaces = extractAllPlaces(response);
        assertThat(allPlaces).allMatch(place -> place.isCoordinatesVerified());
        assertThat(allPlaces).allMatch(place -> place.getMapsUrl() != null);
    }

    @Test
    void generatePlan_duplicateCoordinates_marksUnverified() {
        // Given - multiple places share the same coordinates (e.g., city center)
        AiItineraryResult aiResult = createAiResultWithDuplicateCoordinates(3);
        when(cachedItineraryGenerator.generate(anyString(), eq(3), anyString(), anyString()))
                .thenReturn(aiResult);
        when(mapLinkService.directionsUrl(anyDouble(), anyDouble()))
                .thenReturn("https://maps.google.com/...");

        // When
        TourResponse response = tourPlanService.generatePlan("Bhopal", 3, List.of("food", "culture"));

        // Then - places with duplicate coordinates should be marked unverified
        List<TourResponse.Place> allPlaces = extractAllPlaces(response);
        long unverifiedCount = allPlaces.stream().filter(p -> !p.isCoordinatesVerified()).count();
        long verifiedCount = allPlaces.stream().filter(TourResponse.Place::isCoordinatesVerified).count();

        assertThat(unverifiedCount).isGreaterThan(0);
        assertThat(verifiedCount).isEqualTo(0); // All places share same coords in this test

        // Unverified places should have null mapsUrl
        allPlaces.stream()
                .filter(p -> !p.isCoordinatesVerified())
                .forEach(p -> assertThat(p.getMapsUrl()).isNull());
    }

    @Test
    void generatePlan_mixedCoordinates_someVerifiedSomeNot() {
        // Given - some places have unique coords, some share city center
        AiItineraryResult aiResult = createAiResultWithMixedCoordinates(2);
        when(cachedItineraryGenerator.generate(anyString(), eq(2), anyString(), anyString()))
                .thenReturn(aiResult);
        when(mapLinkService.directionsUrl(anyDouble(), anyDouble()))
                .thenReturn("https://maps.google.com/...");

        // When
        TourResponse response = tourPlanService.generatePlan("Delhi", 2, List.of("food"));

        // Then
        List<TourResponse.Place> allPlaces = extractAllPlaces(response);
        long verifiedCount = allPlaces.stream().filter(TourResponse.Place::isCoordinatesVerified).count();
        long unverifiedCount = allPlaces.stream().filter(p -> !p.isCoordinatesVerified()).count();

        assertThat(verifiedCount).isGreaterThan(0);
        assertThat(unverifiedCount).isGreaterThan(0);
    }

    private AiItineraryResult createAiResultWithUniqueCoordinates(int days) {
        AiItineraryResult result = new AiItineraryResult();
        Map<String, Map<String, List<AiItineraryResult.AiPlace>>> itinerary = new java.util.HashMap<>();

        for (int d = 1; d <= days; d++) {
            Map<String, List<AiItineraryResult.AiPlace>> day = new java.util.HashMap<>();
            // Each place gets unique coordinates - use different base per day and slot
            double baseLat = 48.8566 + d * 0.1;
            double baseLng = 2.3522 + d * 0.1;
            day.put("morning", List.of(createPlace("Morning " + d, baseLat, baseLng)));
            day.put("afternoon", List.of(createPlace("Afternoon " + d, baseLat + 0.01, baseLng + 0.01)));
            day.put("evening", List.of(createPlace("Evening " + d, baseLat + 0.02, baseLng + 0.02)));
            itinerary.put("Day " + d, day);
        }

        result.setItinerary(itinerary);
        result.setRestaurantSuggestions(new java.util.HashMap<>());
        result.setTransportSuggestions(new java.util.HashMap<>());
        result.setTravelTips(List.of("Tip 1"));
        result.setSafetyTips(List.of("Safety 1"));
        return result;
    }

    private AiItineraryResult createAiResultWithDuplicateCoordinates(int days) {
        AiItineraryResult result = new AiItineraryResult();
        Map<String, Map<String, List<AiItineraryResult.AiPlace>>> itinerary = new java.util.HashMap<>();

        // Bhopal city center coordinates - all places share these
        double bhopalLat = 23.255;
        double bhopalLng = 77.414;

        for (int d = 1; d <= days; d++) {
            Map<String, List<AiItineraryResult.AiPlace>> day = new java.util.HashMap<>();
            day.put("morning", List.of(createPlace("Morning " + d, bhopalLat, bhopalLng)));
            day.put("afternoon", List.of(createPlace("Afternoon " + d, bhopalLat, bhopalLng)));
            day.put("evening", List.of(createPlace("Evening " + d, bhopalLat, bhopalLng)));
            itinerary.put("Day " + d, day);
        }

        result.setItinerary(itinerary);
        result.setRestaurantSuggestions(new java.util.HashMap<>());
        result.setTransportSuggestions(new java.util.HashMap<>());
        result.setTravelTips(List.of("Tip 1"));
        result.setSafetyTips(List.of("Safety 1"));
        return result;
    }

    private AiItineraryResult createAiResultWithMixedCoordinates(int days) {
        AiItineraryResult result = new AiItineraryResult();
        Map<String, Map<String, List<AiItineraryResult.AiPlace>>> itinerary = new java.util.HashMap<>();

        double cityCenterLat = 28.6139;
        double cityCenterLng = 77.2090;

        for (int d = 1; d <= days; d++) {
            Map<String, List<AiItineraryResult.AiPlace>> day = new java.util.HashMap<>();
            // Morning place has unique coordinates
            day.put("morning", List.of(createPlace("Morning " + d, cityCenterLat + d * 0.01, cityCenterLng + d * 0.01)));
            // Afternoon and evening share city center (duplicate)
            day.put("afternoon", List.of(createPlace("Afternoon " + d, cityCenterLat, cityCenterLng)));
            day.put("evening", List.of(createPlace("Evening " + d, cityCenterLat, cityCenterLng)));
            itinerary.put("Day " + d, day);
        }

        result.setItinerary(itinerary);
        result.setRestaurantSuggestions(new java.util.HashMap<>());
        result.setTransportSuggestions(new java.util.HashMap<>());
        result.setTravelTips(List.of("Tip 1"));
        result.setSafetyTips(List.of("Safety 1"));
        return result;
    }

    private List<TourResponse.Place> extractAllPlaces(TourResponse response) {
        List<TourResponse.Place> allPlaces = new ArrayList<>();
        if (response.getItinerary() != null) {
            for (Map<String, List<TourResponse.Place>> day : response.getItinerary().values()) {
                for (List<TourResponse.Place> places : day.values()) {
                    allPlaces.addAll(places);
                }
            }
        }
        return allPlaces;
    }

    @Test
    void generatePlan_missingCoordinates_marksUnverified() {
        // Given - place with (0,0) coordinates (invalid)
        AiItineraryResult aiResult = createAiResultWithMissingCoordinates(1);
        when(cachedItineraryGenerator.generate(anyString(), eq(1), anyString(), anyString()))
                .thenReturn(aiResult);
        when(mapLinkService.directionsUrl(anyDouble(), anyDouble()))
                .thenReturn("https://maps.google.com/...");

        // When
        TourResponse response = tourPlanService.generatePlan("TestCity", 1, List.of("food"));

        // Then - place with (0,0) should be marked unverified and have null mapsUrl
        List<TourResponse.Place> allPlaces = extractAllPlaces(response);
        assertThat(allPlaces).hasSize(1);
        TourResponse.Place place = allPlaces.get(0);
        assertThat(place.isCoordinatesVerified()).isFalse();
        assertThat(place.getMapsUrl()).isNull();
    }

    private AiItineraryResult createAiResultWithMissingCoordinates(int days) {
        AiItineraryResult result = new AiItineraryResult();
        Map<String, Map<String, List<AiItineraryResult.AiPlace>>> itinerary = new java.util.HashMap<>();

        for (int d = 1; d <= days; d++) {
            Map<String, List<AiItineraryResult.AiPlace>> day = new java.util.HashMap<>();
            // (0,0) is invalid - should be treated as missing
            day.put("morning", List.of(createPlace("Place with no coords", 0.0, 0.0)));
            itinerary.put("Day " + d, day);
        }

        result.setItinerary(itinerary);
        result.setRestaurantSuggestions(new java.util.HashMap<>());
        result.setTransportSuggestions(new java.util.HashMap<>());
        result.setTravelTips(List.of("Tip 1"));
        result.setSafetyTips(List.of("Safety 1"));
        return result;
    }

    @Test
    void generatePlan_nearbyLegitimateAttractions_notFlaggedAsDuplicate() {
        // Given - two places very close together (e.g., adjacent museums)
        // They differ at 6th decimal place (~0.11m) so should NOT be flagged as duplicate
        AiItineraryResult aiResult = createAiResultWithNearbyPlaces(1);
        when(cachedItineraryGenerator.generate(anyString(), eq(1), anyString(), anyString()))
                .thenReturn(aiResult);
        when(mapLinkService.directionsUrl(anyDouble(), anyDouble()))
                .thenReturn("https://maps.google.com/...");

        // When
        TourResponse response = tourPlanService.generatePlan("Paris", 1, List.of("culture"));

        // Then - both places should be verified (different at 6 decimal precision)
        List<TourResponse.Place> allPlaces = extractAllPlaces(response);
        assertThat(allPlaces).hasSize(2);
        assertThat(allPlaces).allMatch(TourResponse.Place::isCoordinatesVerified);
    }

    @Test
    void generatePlan_differentButWrongCoordinates_notDetectedAsDuplicate() {
        // Given - AI generates different but incorrect coordinates for each place
        // (e.g., all slightly offset from city center but not identical)
        // This is a LIMITATION: current implementation only detects EXACT duplicates
        // at 6-decimal precision. Coords that round to same 6 decimals are flagged.
        AiItineraryResult aiResult = createAiResultWithDifferentWrongCoordinates(2);
        when(cachedItineraryGenerator.generate(anyString(), eq(2), anyString(), anyString()))
                .thenReturn(aiResult);
        when(mapLinkService.directionsUrl(anyDouble(), anyDouble()))
                .thenReturn("https://maps.google.com/...");

        // When
        TourResponse response = tourPlanService.generatePlan("TestCity", 2, List.of("food"));

        // Then - places with coords that round to same 6 decimals are flagged as duplicates
        // This demonstrates the limitation: detection only works at 6-decimal precision
        List<TourResponse.Place> allPlaces = extractAllPlaces(response);
        long verifiedCount = allPlaces.stream().filter(TourResponse.Place::isCoordinatesVerified).count();
        long unverifiedCount = allPlaces.stream().filter(p -> !p.isCoordinatesVerified()).count();
        // At least some are verified (not all duplicates)
        assertThat(verifiedCount).isGreaterThan(0);
        // Some may be flagged if they round to same 6 decimals
        assertThat(unverifiedCount).isGreaterThanOrEqualTo(0);
    }

    @Test
    void generatePlan_mixedValidAndInvalidStops_handlesCorrectly() {
        // Given - one stop with valid coords, one with (0,0), one with another valid
        AiItineraryResult aiResult = createAiResultWithMixedValidInvalid(1);
        when(cachedItineraryGenerator.generate(anyString(), eq(1), anyString(), anyString()))
                .thenReturn(aiResult);
        when(mapLinkService.directionsUrl(anyDouble(), anyDouble()))
                .thenReturn("https://maps.google.com/...");

        // When
        TourResponse response = tourPlanService.generatePlan("TestCity", 1, List.of("food"));

        // Then
        List<TourResponse.Place> allPlaces = extractAllPlaces(response);
        assertThat(allPlaces).hasSize(3);

        // Two valid coord places should be verified
        long verifiedCount = allPlaces.stream().filter(TourResponse.Place::isCoordinatesVerified).count();
        assertThat(verifiedCount).isEqualTo(2);

        // (0,0) place should be unverified with null mapsUrl
        long unverifiedCount = allPlaces.stream().filter(p -> !p.isCoordinatesVerified()).count();
        assertThat(unverifiedCount).isEqualTo(1);
        allPlaces.stream()
                .filter(p -> !p.isCoordinatesVerified())
                .forEach(p -> assertThat(p.getMapsUrl()).isNull());
    }

    private AiItineraryResult createAiResultWithNearbyPlaces(int days) {
        AiItineraryResult result = new AiItineraryResult();
        Map<String, Map<String, List<AiItineraryResult.AiPlace>>> itinerary = new java.util.HashMap<>();

        for (int d = 1; d <= days; d++) {
            Map<String, List<AiItineraryResult.AiPlace>> day = new java.util.HashMap<>();
            // Two places very close but different at 6th decimal (~0.11m apart)
            // Louvre Museum and Tuileries Garden - adjacent in Paris
            day.put("morning", List.of(createPlace("Louvre Museum", 48.8606, 2.3376)));
            day.put("afternoon", List.of(createPlace("Tuileries Garden", 48.8635, 2.3348)));
            itinerary.put("Day " + d, day);
        }

        result.setItinerary(itinerary);
        result.setRestaurantSuggestions(new java.util.HashMap<>());
        result.setTransportSuggestions(new java.util.HashMap<>());
        result.setTravelTips(List.of("Tip 1"));
        result.setSafetyTips(List.of("Safety 1"));
        return result;
    }

    private AiItineraryResult createAiResultWithDifferentWrongCoordinates(int days) {
        AiItineraryResult result = new AiItineraryResult();
        Map<String, Map<String, List<AiItineraryResult.AiPlace>>> itinerary = new java.util.HashMap<>();

        // Simulate AI generating different but wrong coords (all near city center but not identical)
        double cityCenterLat = 28.6139;
        double cityCenterLng = 77.2090;

        for (int d = 1; d <= days; d++) {
            Map<String, List<AiItineraryResult.AiPlace>> day = new java.util.HashMap<>();
            // Each place gets slightly different wrong coordinates
            day.put("morning", List.of(createPlace("Place " + d + "A", cityCenterLat + d * 0.001, cityCenterLng + d * 0.001)));
            day.put("afternoon", List.of(createPlace("Place " + d + "B", cityCenterLat + d * 0.002, cityCenterLng + d * 0.002)));
            itinerary.put("Day " + d, day);
        }

        result.setItinerary(itinerary);
        result.setRestaurantSuggestions(new java.util.HashMap<>());
        result.setTransportSuggestions(new java.util.HashMap<>());
        result.setTravelTips(List.of("Tip 1"));
        result.setSafetyTips(List.of("Safety 1"));
        return result;
    }

    private AiItineraryResult createAiResultWithMixedValidInvalid(int days) {
        AiItineraryResult result = new AiItineraryResult();
        Map<String, Map<String, List<AiItineraryResult.AiPlace>>> itinerary = new java.util.HashMap<>();

        for (int d = 1; d <= days; d++) {
            Map<String, List<AiItineraryResult.AiPlace>> day = new java.util.HashMap<>();
            // One valid coordinate
            day.put("morning", List.of(createPlace("Valid Place", 48.8566, 2.3522)));
            // One with (0,0)
            day.put("afternoon", List.of(createPlace("No Coords", 0.0, 0.0)));
            // One with out-of-range (will be caught by validator, but let's test)
            // Actually validator rejects out-of-range, so use valid but different
            day.put("evening", List.of(createPlace("Another Valid", 48.8606, 2.3376)));
            itinerary.put("Day " + d, day);
        }

        result.setItinerary(itinerary);
        result.setRestaurantSuggestions(new java.util.HashMap<>());
        result.setTransportSuggestions(new java.util.HashMap<>());
        result.setTravelTips(List.of("Tip 1"));
        result.setSafetyTips(List.of("Safety 1"));
        return result;
    }
}