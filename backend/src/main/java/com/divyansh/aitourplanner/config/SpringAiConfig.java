package com.divyansh.aitourplanner.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Wires Spring AI's ChatClient to OpenRouter's OpenAI-compatible endpoint.
 *
 * We build the OpenAiApi/OpenAiChatModel manually (instead of relying purely
 * on Spring AI's OpenAI auto-configuration) so we can attach the two extra
 * headers OpenRouter uses for attribution (HTTP-Referer, X-Title) - the
 * same headers the previous raw WebClient integration sent.
 */
@Configuration
public class SpringAiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${openrouter.model.name}")
    private String modelName;

    @Bean
    public OpenAiApi openAiApi() {
        MultiValueMap<String, String> extraHeaders = new LinkedMultiValueMap<>();
        extraHeaders.add("HTTP-Referer", "https://raahi.ai");
        extraHeaders.add("X-Title", "Raahi-Tour-Planner");

        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .headers(extraHeaders)
                .build();
    }

    @Bean
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(modelName)
                        .temperature(0.7)
                        .build())
                .build();
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }
}