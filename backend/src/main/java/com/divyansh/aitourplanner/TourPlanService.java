package com.divyansh.aitourplanner;

import com.divyansh.aitourplanner.model.TourResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.ClientResponse;

import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class TourPlanService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.model.name}")
    private String modelname;

    @Value("${openrouter.prompt}")
private String promptTemplate;


    private final WebClient webClient;

    public TourPlanService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    // ---------------------------------------------------------
    //                  MAIN FUNCTION
    // ---------------------------------------------------------
    public TourResponse generatePlan(String city, int days, List<String> interests) {

        String prompt = String.format(promptTemplate, days, city);

        Map<String, Object> requestBody = Map.of(
                "model", modelname,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        Map<String, Map<String, List<TourResponse.Place>>> itinerary;

        try {
            String response = callApiWithRetry(requestBody);
            System.out.println("RAW == " + response);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

           
            String content = extractContent(root);
            System.out.println("CONTENT == " + content);

            // extract only JSON
            int s = content.indexOf("{");
            int e = content.lastIndexOf("}");

            if (s == -1 || e == -1) {
                throw new Exception("No JSON found!");
            }

            String json = content.substring(s, e + 1);
            System.out.println("FINAL JSON BLOCK == " + json);

            JsonNode itineraryNode = mapper.readTree(json).path("itinerary");

            itinerary = mapper.convertValue(
                    itineraryNode,
                    new TypeReference<>() {}
            );

            fillMissing(days, itinerary);

        } catch (Exception ex) {
            ex.printStackTrace();
            itinerary = fallback(days);
        }

        return new TourResponse(city, days, interests, itinerary);
    }

    // ---------------------------------------------------------
    //            API CALL + RETRY
    // ---------------------------------------------------------
    private String callApiWithRetry(Map<String, Object> body) throws Exception {

        int retries = 0;

        while (retries < 5) {

            try {
                return webClient.post()
                        .uri("https://openrouter.ai/api/v1/chat/completions")
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .header("HTTP-Referer", "https://raahi.ai")
                        .header("X-Title", "Raahi-Tour-Planner")
                        .bodyValue(body)
                        .exchangeToMono(this::handleResponse)
                        .block();

            } catch (WebClientResponseException ex) {

                if (ex.getStatusCode().value() == 429) {
                    retries++;
                    long wait = (long) Math.pow(2, retries) * 2000;
                    Thread.sleep(wait);
                } else {
                    throw ex;
                }

            }
        }

        throw new Exception("Failed after retries");
    }

    private Mono<String> handleResponse(ClientResponse response) {
        if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(String.class);
        }
        return response.createException().flatMap(Mono::error);
    }

    // ---------------------------------------------------------
    //            EXTRACT CONTENT FROM ANY MODEL FORMAT
    // ---------------------------------------------------------
    private String extractContent(JsonNode root) {

        JsonNode choice = root.path("choices").get(0);

        if (choice.has("message") && choice.get("message").has("content"))
            return choice.get("message").get("content").asText();

        if (choice.has("text"))
            return choice.get("text").asText();

        if (choice.has("delta") && choice.get("delta").has("content"))
            return choice.get("delta").get("content").asText();

        return "";
    }

    // ---------------------------------------------------------
    //              FIX MISSING VALUES
    // ---------------------------------------------------------
    private void fillMissing(int days,
                             Map<String, Map<String, List<TourResponse.Place>>> map) {

        for (int i = 1; i <= days; i++) {

            String key = "Day " + i;
            map.putIfAbsent(key, new HashMap<>());

            Map<String, List<TourResponse.Place>> day = map.get(key);

            ensureSlot(day, "morning");
            ensureSlot(day, "afternoon");
            ensureSlot(day, "evening");
        }
    }

    private void ensureSlot(Map<String, List<TourResponse.Place>> day, String slot) {
        if (!day.containsKey(slot)) {
            day.put(slot, List.of(
                    new TourResponse.Place("No plan", "Unavailable", 0, 0)
            ));
        }
    }

    // ---------------------------------------------------------
    //            FALLBACK (SAFE EMPTY STRUCTURE)
    // ---------------------------------------------------------
    private Map<String, Map<String, List<TourResponse.Place>>> fallback(int days) {

        Map<String, Map<String, List<TourResponse.Place>>> map = new HashMap<>();

        for (int i = 1; i <= days; i++) {

            Map<String, List<TourResponse.Place>> day = new HashMap<>();

            day.put("morning", List.of(new TourResponse.Place("No data", "Error", 0, 0)));
            day.put("afternoon", List.of(new TourResponse.Place("No data", "Error", 0, 0)));
            day.put("evening", List.of(new TourResponse.Place("No data", "Error", 0, 0)));

            map.put("Day " + i, day);
        }
        return map;
    }
} 
