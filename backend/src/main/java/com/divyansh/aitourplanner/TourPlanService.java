package com.divyansh.aitourplanner;

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
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI itinerary generation, backed by Spring AI's ChatClient (pointed at
 * OpenRouter - see SpringAiConfig) instead of the previous raw WebClient
 * call. Public method signatures are unchanged so TourPlanController and the
 * existing frontend contract keep working.
 */
@Service
public class TourPlanService {

    private static final Logger log = LoggerFactory.getLogger(TourPlanService.class);

    @Value("${openrouter.prompt}")
    private String promptTemplate;

    private final ChatClient chatClient;
    private final CachedItineraryGenerator cachedItineraryGenerator;
    private final ItineraryValidator validator;
    private final MapLinkService mapLinkService;
    private final ItineraryTools itineraryTools;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GUARDRAILS = """
            Do not state opening hours, prices, ticket availability, or booking \
            requirements as guaranteed facts - if you mention them, phrase them as \
            typical/approximate and advise the traveler to verify before visiting.""";

    public TourPlanService(ChatClient chatClient,
                            CachedItineraryGenerator cachedItineraryGenerator,
                            ItineraryValidator validator,
                            MapLinkService mapLinkService,
                            ItineraryTools itineraryTools) {
        this.chatClient = chatClient;
        this.cachedItineraryGenerator = cachedItineraryGenerator;
        this.validator = validator;
        this.mapLinkService = mapLinkService;
        this.itineraryTools = itineraryTools;
    }

    // ---------------------------------------------------------
    //                  MAIN FUNCTION (existing endpoint)
    // ---------------------------------------------------------
    public TourResponse generatePlan(String city, int days, List<String> interests) {
        long totalStart = System.nanoTime();
        long aiStart, aiEnd, validationStart, validationEnd, conversionStart, conversionEnd;

        try {
            String prompt = buildGenerationPrompt(city, days, interests);
            String cacheCity = city == null ? "" : city.trim().toLowerCase().replaceAll("\\s+", " ");
            String interestsKey = normalizeInterests(interests);

            aiStart = System.nanoTime();
            AiItineraryResult aiResult = cachedItineraryGenerator.generate(cacheCity, days, interestsKey, prompt);
            aiEnd = System.nanoTime();

            validationStart = System.nanoTime();
            // Enforce exact day count after validation
            enforceDayCount(aiResult, days);

            fillMissingSlots(days, aiResult);
            validationEnd = System.nanoTime();

            conversionStart = System.nanoTime();
            TourResponse response = toTourResponse(city, days, interests, aiResult);
            conversionEnd = System.nanoTime();

            log.info("Timing (ms) for city={}, days={}: total={}, ai={}, validation+fill={}, conversion={}",
                    city, days,
                    (System.nanoTime() - totalStart) / 1_000_000,
                    (aiEnd - aiStart) / 1_000_000,
                    (validationEnd - validationStart) / 1_000_000,
                    (conversionEnd - conversionStart) / 1_000_000);

            return response;

        } catch (ItineraryValidationException ex) {
            log.error("Itinerary validation failed for city={}, days={}: {}", city, days, ex.getMessage());
            throw ex;
        } catch (AiServiceException ex) {
            log.error("AI service error for city={}, days={}: {}", city, days, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error generating tour plan for city={}, days={}: {}", city, days, ex.getMessage(), ex);
            throw new AiServiceException("Failed to generate itinerary. Please try again.", ex);
        }
    }

    /** Order/case-independent so "food, culture" and "Culture, Food" hit the same cache entry. */
    private String normalizeInterests(List<String> interests) {
        if (interests == null || interests.isEmpty()) return "";
        return interests.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.trim().toLowerCase())
                .distinct()
                .sorted()
                .collect(Collectors.joining(","));
    }

    private String buildGenerationPrompt(String city, int days, List<String> interests) {
        // Sanitize user inputs to prevent prompt injection
        String safeCity = sanitizeForPrompt(city);
        String safeInterests = interests == null || interests.isEmpty()
                ? ""
                : "Traveler interests: " + interests.stream()
                        .filter(s -> s != null && !s.isBlank())
                        .map(this::sanitizeForPrompt)
                        .collect(Collectors.joining(", ")) + ".";

        String base = String.format(promptTemplate, days, safeCity);

        return base + "\n" + safeInterests
                + "\nFor each day, include a morning, afternoon and evening activity with a short "
                + "description and coordinates, plus restaurant suggestions, transport suggestions, "
                + "general travel tips and safety tips."
                + "\nCoordinates MUST be in decimal degrees (latitude -90 to 90, longitude -180 to 180). "
                + "Do NOT use (0,0) as a placeholder. If unsure about coordinates, omit the place."
                + "\n" + GUARDRAILS;
    }

    /**
     * Sanitize user input for safe inclusion in prompts.
     * Removes control characters and limits length.
     */
    private String sanitizeForPrompt(String input) {
        if (input == null) return "";
        // Remove control characters, limit length
        String sanitized = input.replaceAll("[\\p{Cntrl}&&[^\\n\\r\\t]]", "")
                    .trim();
        return sanitized.substring(0, Math.min(sanitized.length(), 200));
    }

    // ---------------------------------------------------------
    //          AI ITINERARY MODIFICATION (new)
    // ---------------------------------------------------------
    public TourResponse modifyItinerary(ModifyItineraryRequest request) {
        try {
            String currentItineraryJson = objectMapper.writeValueAsString(request.getCurrentItinerary());
            String interestsLine = (request.getInterests() == null || request.getInterests().isEmpty())
                    ? "none specified"
                    : request.getInterests().stream()
                            .filter(s -> s != null && !s.isBlank())
                            .map(this::sanitizeForPrompt)
                            .collect(Collectors.joining(", "));

            String safeCity = sanitizeForPrompt(request.getCity());
            String safeInstruction = sanitizeForPrompt(request.getInstruction());

            String prompt = """
                    You are editing an existing %d-day travel itinerary for %s.
                    Traveler interests: %s.

                    Current itinerary as JSON:
                    %s

                    Apply this requested change: "%s"

                    Only change what the instruction requires; keep everything else from the \
                    current itinerary as-is. Return the full updated itinerary in the same \
                    structure (all %d days), including restaurant suggestions, transport \
                    suggestions, travel tips and safety tips.
                    %s
                    """.formatted(request.getDays(), safeCity, interestsLine,
                    currentItineraryJson, safeInstruction, request.getDays(), GUARDRAILS);

            AiItineraryResult result = chatClient.prompt()
                    .user(prompt)
                    .tools(itineraryTools)
                    .call()
                    .entity(AiItineraryResult.class);

            validator.validateAndClean(result, request.getDays());
            enforceDayCount(result, request.getDays());
            fillMissingSlots(request.getDays(), result);

            return toTourResponse(request.getCity(), request.getDays(), request.getInterests(), result);

        } catch (ItineraryValidationException ex) {
            log.error("Itinerary validation failed during modification: {}", ex.getMessage());
            throw ex;
        } catch (AiServiceException ex) {
            log.error("AI service error during modification: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error modifying itinerary: {}", ex.getMessage(), ex);
            throw new ItineraryOperationException("Could not modify the itinerary right now.", ex);
        }
    }

    // ---------------------------------------------------------
    //          AI TRAVEL ASSISTANT (new)
    // ---------------------------------------------------------
    public AssistantResponse assistantChat(AssistantRequest request) {
        try {
            String currentItineraryJson = objectMapper.writeValueAsString(request.getCurrentItinerary());
            String interestsLine = (request.getInterests() == null || request.getInterests().isEmpty())
                    ? "none specified"
                    : request.getInterests().stream()
                            .filter(s -> s != null && !s.isBlank())
                            .map(this::sanitizeForPrompt)
                            .collect(Collectors.joining(", "));

            String safeCity = sanitizeForPrompt(request.getCity());
            String safeQuestion = sanitizeForPrompt(request.getQuestion());

            String prompt = """
                    You are a helpful, concise travel assistant for a trip to %s.
                    Traveler interests: %s.

                    Current itinerary as JSON:
                    %s

                    Use the available tools for any distance, travel-time, or map-link needs \
                    instead of guessing. Keep your answer short and practical (a few sentences \
                    or a short list).
                    %s

                    Traveler's question: %s
                    """.formatted(safeCity, interestsLine, currentItineraryJson,
                    GUARDRAILS, safeQuestion);

            String answer = chatClient.prompt()
                    .user(prompt)
                    .tools(itineraryTools)
                    .call()
                    .content();

            return new AssistantResponse(answer);

        } catch (AiServiceException ex) {
            log.error("AI service error in assistant: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error in travel assistant: {}", ex.getMessage(), ex);
            throw new AiServiceException("The travel assistant is unavailable right now.", ex);
        }
    }

    /**
     * Ensures the AI result contains exactly the expected number of days.
     * If days are missing, they will be added by fillMissingSlots.
     * If extra days exist (beyond expected), they are removed.
     */
    private void enforceDayCount(AiItineraryResult aiResult, int expectedDays) {
        if (aiResult.getItinerary() == null) return;

        Map<String, Map<String, List<AiItineraryResult.AiPlace>>> itinerary = aiResult.getItinerary();

        // Remove any days beyond expected range
        itinerary.keySet().removeIf(dayKey -> !isValidDayKey(dayKey, expectedDays));

        // Log if day count doesn't match
        if (itinerary.size() != expectedDays) {
            log.warn("AI returned {} valid days after validation, expected {}", itinerary.size(), expectedDays);
        }
    }

    private boolean isValidDayKey(String dayKey, int expectedDays) {
        if (dayKey == null) return false;
        String trimmed = dayKey.trim();
        if (!trimmed.matches("(?i)day\\s*\\d+")) return false;
        String digits = trimmed.replaceAll("\\D", "");
        try {
            int n = Integer.parseInt(digits);
            return n >= 1 && n <= expectedDays;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ---------------------------------------------------------
    //      CONVERSION: AiItineraryResult -> TourResponse
    // ---------------------------------------------------------
    private TourResponse toTourResponse(String city, int days, List<String> interests, AiItineraryResult ai) {
        Map<String, Map<String, List<TourResponse.Place>>> itinerary = new HashMap<>();

        List<TourResponse.Place> allPlaces = new ArrayList<>();
        Map<String, Integer> coordinateCounts = new HashMap<>();

        for (var dayEntry : ai.getItinerary().entrySet()) {
            Map<String, List<TourResponse.Place>> day = new HashMap<>();
            for (var slotEntry : dayEntry.getValue().entrySet()) {
                List<TourResponse.Place> places = slotEntry.getValue().stream()
                        .map(this::toPlace)
                        .peek(p -> allPlaces.add(p))
                        .toList();
                day.put(slotEntry.getKey(), places);
            }
            itinerary.put(dayEntry.getKey(), day);
        }

        for (TourResponse.Place place : allPlaces) {
            if (place.getLat() != 0.0 || place.getLng() != 0.0) {
                String coordKey = String.format("%.6f,%.6f", place.getLat(), place.getLng());
                coordinateCounts.merge(coordKey, 1, Integer::sum);
            }
        }

        Set<String> duplicateCoords = coordinateCounts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        for (TourResponse.Place place : allPlaces) {
            if (place.getLat() != 0.0 || place.getLng() != 0.0) {
                String coordKey = String.format("%.6f,%.6f", place.getLat(), place.getLng());
                boolean isDuplicate = duplicateCoords.contains(coordKey);
                place.setCoordinatesVerified(!isDuplicate);
                if (isDuplicate) {
                    place.setMapsUrl(null);
                }
            } else {
                place.setCoordinatesVerified(false);
                place.setMapsUrl(null);
            }
        }

        return new TourResponse(city, days, interests, itinerary,
                ai.getRestaurantSuggestions(), ai.getTransportSuggestions(),
                ai.getTravelTips(), ai.getSafetyTips());
    }

    private TourResponse.Place toPlace(AiItineraryResult.AiPlace aiPlace) {
        TourResponse.Place place = new TourResponse.Place(
                aiPlace.getName(), aiPlace.getDescription(), aiPlace.getLat(), aiPlace.getLng());
        place.setCoordinatesVerified(aiPlace.isCoordinatesVerified());
        String mapsUrl = mapLinkService.directionsUrl(aiPlace.getLat(), aiPlace.getLng());
        place.setMapsUrl(mapsUrl);
        return place;
    }

    // ---------------------------------------------------------
    //            FIX MISSING VALUES (kept from original)
    // ---------------------------------------------------------
    private void fillMissingSlots(int days, AiItineraryResult ai) {
        Map<String, Map<String, List<AiItineraryResult.AiPlace>>> map = ai.getItinerary();

        for (int i = 1; i <= days; i++) {
            String key = "Day " + i;
            map.putIfAbsent(key, new HashMap<>());
            Map<String, List<AiItineraryResult.AiPlace>> day = map.get(key);

            ensureSlot(day, "morning");
            ensureSlot(day, "afternoon");
            ensureSlot(day, "evening");
        }
    }

    private void ensureSlot(Map<String, List<AiItineraryResult.AiPlace>> day, String slot) {
        if (!day.containsKey(slot) || day.get(slot) == null || day.get(slot).isEmpty()) {
            // Don't add placeholder places with (0,0) coordinates.
            // The frontend handles empty slots gracefully.
            day.put(slot, List.of());
        }
    }
}