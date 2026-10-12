package com.divyansh.aitourplanner.service;

import com.divyansh.aitourplanner.exception.custom.ItineraryValidationException;
import com.divyansh.aitourplanner.model.AiItineraryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates the structured output the LLM returns before it's converted into
 * a TourResponse. Invalid individual places are dropped rather than failing
 * the whole request; a completely empty/unusable result throws so the caller
 * can fall back to the safe placeholder itinerary.
 */
@Component
public class ItineraryValidator {

    private static final Logger log = LoggerFactory.getLogger(ItineraryValidator.class);

    // Reasonable limits to prevent excessive responses
    private static final int MAX_PLACES_PER_SLOT = 10;
    private static final int MAX_TOTAL_PLACES_PER_DAY = 30;
    private static final int MAX_RESTAURANT_SUGGESTIONS_PER_DAY = 20;
    private static final int MAX_TRANSPORT_SUGGESTIONS_PER_DAY = 20;
    private static final int MAX_TRAVEL_TIPS = 50;
    private static final int MAX_SAFETY_TIPS = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_NAME_LENGTH = 200;

    private final MapLinkService mapLinkService;

    public ItineraryValidator(MapLinkService mapLinkService) {
        this.mapLinkService = mapLinkService;
    }

    /**
     * Cleans the result in place (removing invalid places/days) and returns
     * it. Throws ItineraryValidationException if nothing usable is left.
     */
    public AiItineraryResult validateAndClean(AiItineraryResult result, int expectedDays) {
        if (result == null || result.getItinerary() == null || result.getItinerary().isEmpty()) {
            throw new ItineraryValidationException("AI response contained no itinerary structure");
        }

        Map<String, Map<String, List<AiItineraryResult.AiPlace>>> itinerary = result.getItinerary();

        // Drop any day keys that aren't parseable as "Day N" within range -
        // guards against the AI inventing day numbers that don't exist.
        itinerary.keySet().removeIf(dayKey -> !isValidDayKey(dayKey, expectedDays));

        // Validate that we have exactly the expected number of days (after cleaning invalid keys)
        if (itinerary.size() != expectedDays) {
            log.warn("AI returned {} valid days, expected {}", itinerary.size(), expectedDays);
        }

        int usablePlaceCount = 0;

        for (Map<String, List<AiItineraryResult.AiPlace>> day : itinerary.values()) {
            if (day == null) continue;

            // Validate each time slot (morning, afternoon, evening)
            for (Map.Entry<String, List<AiItineraryResult.AiPlace>> slotEntry : day.entrySet()) {
                String slot = slotEntry.getKey();
                List<AiItineraryResult.AiPlace> places = slotEntry.getValue();

                if (places == null) continue;

                // Limit places per slot
                if (places.size() > MAX_PLACES_PER_SLOT) {
                    log.warn("Truncating places for slot {}: {} -> {}", slot, places.size(), MAX_PLACES_PER_SLOT);
                    places.subList(MAX_PLACES_PER_SLOT, places.size()).clear();
                }

                // Validate individual places
                places.removeIf(p -> !isValidPlace(p));
                usablePlaceCount += places.size();
            }

            // Limit total places per day
            int dayTotal = day.values().stream().mapToInt(List::size).sum();
            if (dayTotal > MAX_TOTAL_PLACES_PER_DAY) {
                log.warn("Day has too many places ({}), truncating", dayTotal);
                truncateDayPlaces(day, MAX_TOTAL_PLACES_PER_DAY);
            }
        }

        // Validate and clean additional sections
        validateAndLimitMap(result.getRestaurantSuggestions(), MAX_RESTAURANT_SUGGESTIONS_PER_DAY, "restaurant suggestions");
        validateAndLimitMap(result.getTransportSuggestions(), MAX_TRANSPORT_SUGGESTIONS_PER_DAY, "transport suggestions");
        validateAndLimitList(result.getTravelTips(), MAX_TRAVEL_TIPS, "travel tips");
        validateAndLimitList(result.getSafetyTips(), MAX_SAFETY_TIPS, "safety tips");

        if (usablePlaceCount == 0) {
            throw new ItineraryValidationException("AI response had no valid places (bad coordinates/names)");
        }

        return result;
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

    private boolean isValidPlace(AiItineraryResult.AiPlace place) {
        if (place == null) return false;
        if (place.getName() == null || place.getName().isBlank()) return false;
        if (place.getName().length() > MAX_NAME_LENGTH) return false;
        if (place.getDescription() != null && place.getDescription().length() > MAX_DESCRIPTION_LENGTH) return false;
        return mapLinkService.isValidCoordinate(place.getLat(), place.getLng());
    }

    private void validateAndLimitMap(Map<String, List<String>> map, int maxPerKey, String context) {
        if (map == null) return;
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            List<String> list = entry.getValue();
            if (list == null) continue;
            // Limit items per day
            if (list.size() > maxPerKey) {
                log.warn("Truncating {} for day {}: {} -> {}", context, entry.getKey(), list.size(), maxPerKey);
                list.subList(maxPerKey, list.size()).clear();
            }
            // Remove blank/null items
            list.removeIf(s -> s == null || s.isBlank());
        }
    }

    private void validateAndLimitList(List<String> list, int max, String context) {
        if (list == null) return;
        if (list.size() > max) {
            log.warn("Truncating {}: {} -> {}", context, list.size(), max);
            list.subList(max, list.size()).clear();
        }
        list.removeIf(s -> s == null || s.isBlank());
    }

    private void truncateDayPlaces(Map<String, List<AiItineraryResult.AiPlace>> day, int maxTotal) {
        // Collect all places with their slot info
        List<SlotPlace> allPlaces = day.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(p -> new SlotPlace(entry.getKey(), p)))
                .collect(Collectors.toList());

        // Keep only valid places
        allPlaces.removeIf(sp -> !isValidPlace(sp.place));

        // Sort by slot priority (morning, afternoon, evening) then take maxTotal
        int kept = 0;
        Set<String> slotsToKeep = Set.of("morning", "afternoon", "evening");
        List<SlotPlace> sorted = allPlaces.stream()
                .sorted((a, b) -> {
                    int idxA = slotsToKeep.contains(a.slot.toLowerCase()) ? 0 : 1;
                    int idxB = slotsToKeep.contains(b.slot.toLowerCase()) ? 0 : 1;
                    if (idxA != idxB) return Integer.compare(idxA, idxB);
                    return slotsToKeep.stream()
                            .filter(s -> s.equalsIgnoreCase(a.slot))
                            .findFirst()
                            .map(s -> 0)
                            .orElse(1) - slotsToKeep.stream()
                            .filter(s -> s.equalsIgnoreCase(b.slot))
                            .findFirst()
                            .map(s -> 0)
                            .orElse(1);
                })
                .limit(maxTotal)
                .collect(Collectors.toList());

        // Rebuild day map with kept places
        day.clear();
        for (SlotPlace sp : sorted) {
            day.computeIfAbsent(sp.slot, k -> new java.util.ArrayList<>()).add(sp.place);
        }
    }

    private record SlotPlace(String slot, AiItineraryResult.AiPlace place) {}
}