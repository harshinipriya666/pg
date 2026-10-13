package com.pgmanagement.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgmanagement.ai.config.AIConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    private final AIConfig aiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AIService(AIConfig aiConfig, RestTemplate aiRestTemplate, ObjectMapper objectMapper) {
        this.aiConfig = aiConfig;
        this.restTemplate = aiRestTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return aiConfig.isConfigured();
    }

    /**
     * Calls Gemini generateContent with user prompt and optional system instruction.
     * Returns null if API key is not configured, API errors occur, or timeout fires.
     */
    public String generateText(String prompt, String systemInstruction) {
        if (!aiConfig.isConfigured()) {
            logger.debug("Gemini API key is not configured. Falling back to deterministic engine.");
            return null;
        }

        try {
            String model = aiConfig.getModel();
            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    model, aiConfig.getApiKey()
            );

            Map<String, Object> requestPayload = new HashMap<>();

            // System instruction if provided
            if (systemInstruction != null && !systemInstruction.isBlank()) {
                requestPayload.put("system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemInstruction))
                ));
            }

            // User prompt contents
            List<Map<String, Object>> contents = new ArrayList<>();
            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", prompt))
            ));
            requestPayload.put("contents", contents);

            // Generation config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.3);
            generationConfig.put("maxOutputTokens", 1200);
            requestPayload.put("generationConfig", generationConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
                    if (!textNode.isMissingNode()) {
                        return textNode.asText().trim();
                    }
                }
            }
        } catch (HttpStatusCodeException e) {
            logger.warn("Gemini API returned HTTP status {}: {}", e.getStatusCode(), e.getStatusText());
        } catch (ResourceAccessException e) {
            logger.warn("Gemini API request timed out or network error: {}", e.getMessage());
        } catch (Exception e) {
            logger.warn("Failed to generate content with Gemini API: {}", e.getMessage());
        }

        return null;
    }
}
