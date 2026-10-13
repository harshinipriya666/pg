package com.pgmanagement.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AIConfig {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

    @Value("${gemini.timeout.seconds:10}")
    private int timeoutSeconds;

    @Bean
    public RestTemplate aiRestTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .readTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.equalsIgnoreCase("YOUR_GEMINI_API_KEY");
    }

    public String getApiKey() {
        return apiKey != null ? apiKey.trim() : "";
    }

    public String getModel() {
        return (model != null && !model.isBlank()) ? model.trim() : "gemini-1.5-flash";
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
