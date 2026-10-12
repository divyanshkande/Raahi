package com.divyansh.aitourplanner.service;

import com.divyansh.aitourplanner.model.AiItineraryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Isolated in its own Spring bean so the @Cacheable proxy actually intercepts
 * the call - calling a @Cacheable method on `this` from within TourPlanService
 * would bypass Spring's caching proxy entirely (self-invocation).
 *
 * Only successful, validated results are cached: validation failures/AI
 * errors throw, and Spring Cache does not cache a method that throws, so a
 * bad response never poisons the cache for later identical requests.
 */
@Component
public class CachedItineraryGenerator {

    private static final Logger log = LoggerFactory.getLogger(CachedItineraryGenerator.class);

    private final ChatClient chatClient;
    private final ItineraryValidator validator;

    public CachedItineraryGenerator(ChatClient chatClient, ItineraryValidator validator) {
        this.chatClient = chatClient;
        this.validator = validator;
    }

    /**
     * @param city         used only for the cache key (case/whitespace normalized by the caller)
     * @param days         used only for the cache key
     * @param interestsKey normalized (sorted, lowercased) interests string, used only for the cache key
     * @param prompt       the fully-built prompt actually sent to the model
     */
    @Cacheable(value = "itineraries", key = "#city + '-' + #days + '-' + #interestsKey")
    public AiItineraryResult generate(String city, int days, String interestsKey, String prompt) {
        long aiStart = System.nanoTime();
        AiItineraryResult result = chatClient.prompt()
                .user(prompt)
                .call()
                .entity(AiItineraryResult.class);
        long aiEnd = System.nanoTime();

        long aiDurationMs = (aiEnd - aiStart) / 1_000_000;
        log.debug("OpenRouter AI call took {} ms for city={}, days={}", aiDurationMs, city, days);

        // Throws on bad output - see class javadoc for why that matters for caching.
        validator.validateAndClean(result, days);
        return result;
    }
}
