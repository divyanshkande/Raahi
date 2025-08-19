package com.divyansh.aitourplanner;

import com.divyansh.aitourplanner.model.TourResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TourPlanService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public TourPlanService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public TourResponse generatePlan(String city, int days, List<String> interests) {
        String interestsStr = String.join(", ", interests);

        String prompt = String.format(
                "Create a detailed %d-day tour itinerary for %s focusing on interests: %s. " +
                        "Return the itinerary strictly in JSON format like below:\n" +
                        "{\n" +
                        "  \"Day 1\": {\"morning\": {}, \"afternoon\": {}, \"evening\": {}, \"foodTip\": \"\", \"transportTip\": \"\"},\n" +
                        "  \"Day 2\": {\"morning\": {}, \"afternoon\": {}, \"evening\": {}, \"foodTip\": \"\", \"transportTip\": \"\"},\n" +
                        "  ...\n" +
                        "}\n" +
                        "Each period should include an activity and description. Also add foodTip and transportTip per day.",
                days, city, interestsStr
        );

        Map<String, Object> requestBody = Map.of(
                "model", "google/gemini-2.0-flash-exp:free",
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        Map<String, Map<String, Object>> itineraryMap;

        try {
            String rawResponse = callApiWithRetry(requestBody);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawResponse);
            String content = root.path("choices").get(0).path("message").path("content").asText().trim();

            // Extract JSON safely
            int start = content.indexOf("{");
            int end = content.lastIndexOf("}");
            if (start == -1 || end == -1) throw new Exception("No valid JSON found in AI response");
            String itineraryJson = content.substring(start, end + 1);

            // Parse JSON into Map<String, Map<String, Object>>
            itineraryMap = mapper.readValue(itineraryJson,
                    new TypeReference<Map<String, Map<String, Object>>>() {});

        } catch (Exception e) {
            e.printStackTrace();
            itineraryMap = generateEmptyItinerary(days);
        }

        return new TourResponse(city, days, interests, itineraryMap);
    }

    private String callApiWithRetry(Map<String, Object> requestBody) throws Exception {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                return webClient.post()
                        .uri("https://openrouter.ai/api/v1/chat/completions")
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .bodyValue(requestBody)
                        .exchangeToMono(this::handleResponse)
                        .block();
            } catch (WebClientResponseException.TooManyRequests ex) {
                attempt++;
                String retryAfter = ex.getHeaders().getFirst("Retry-After");
                long waitSeconds = retryAfter != null ? Long.parseLong(retryAfter) : 5;
                System.out.println("Rate limited. Waiting for " + waitSeconds + " seconds before retrying...");
                Thread.sleep(waitSeconds * 1000);
            }
        }
        throw new Exception("API request failed after " + maxRetries + " retries due to rate limits.");
    }

    private Mono<String> handleResponse(ClientResponse response) {
        if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(String.class);
        } else if (response.statusCode().value() == 429) {
            return response.createException().flatMap(Mono::error);
        } else {
            return response.createException().flatMap(Mono::error);
        }
    }

    private Map<String, Map<String, Object>> generateEmptyItinerary(int days) {
        Map<String, Map<String, Object>> empty = new HashMap<>();
        for (int i = 1; i <= days; i++) {
            Map<String, Object> dayPlan = new HashMap<>();
            dayPlan.put("morning", "No plan available due to error or API limit.");
            dayPlan.put("afternoon", "No plan available due to error or API limit.");
            dayPlan.put("evening", "No plan available due to error or API limit.");
            dayPlan.put("foodTip", "No food tip available.");
            dayPlan.put("transportTip", "No transport tip available.");
            empty.put("Day " + i, dayPlan);
        }
        return empty;
    }
}
