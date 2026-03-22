//package com.nexamed.ai.client;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.Duration;
//import java.util.List;
//import java.util.Map;
//
///**
// * Raw HTTP client for OpenAI Chat Completions API.
// * We use RestTemplate directly (no Spring AI library) so you learn
// * the actual API structure — better for interviews.
// *
// * API Docs: https://platform.openai.com/docs/api-reference/chat
// */
//@Component
//@Slf4j
//public class OpenAIClient {
//
//    @Value("${openai.api.key}")
//    private String apiKey;
//
//    @Value("${openai.api.url}")
//    private String apiUrl;
//
//    @Value("${openai.api.model}")
//    private String model;
//
//    @Value("${openai.api.max-tokens}")
//    private int maxTokens;
//
//    @Value("${openai.api.temperature}")
//    private double temperature;
//
//    private final RestTemplate restTemplate;
//    private final ObjectMapper objectMapper;
//
//    public OpenAIClient(ObjectMapper objectMapper) {
//        this.objectMapper = objectMapper;
//        // Separate RestTemplate with longer timeout for GPT calls
//        this.restTemplate = new RestTemplate();
//    }
//
//    /**
//     * Send a prompt and get back the raw text response.
//     * All service methods call this single method.
//     */
//    public String complete(String systemPrompt, String userPrompt) {
//        long start = System.currentTimeMillis();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(apiKey);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, Object> body = Map.of(
//                "model", model,
//                "messages", List.of(
//                        Map.of("role", "system", "content", systemPrompt),
//                        Map.of("role", "user",   "content", userPrompt)
//                ),
//                "max_tokens",  maxTokens,
//                "temperature", temperature
//        );
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
//
//        try {
//            ResponseEntity<JsonNode> response = restTemplate.exchange(
//                    apiUrl, HttpMethod.POST, entity, JsonNode.class);
//
//            JsonNode root = response.getBody();
//            if (root == null) throw new RuntimeException("Empty response from OpenAI");
//
//            // Extract text from: choices[0].message.content
//            String content = root
//                    .path("choices").get(0)
//                    .path("message")
//                    .path("content")
//                    .asText();
//
//            long elapsed = System.currentTimeMillis() - start;
//            log.info("OpenAI response received in {}ms, tokens used: {}",
//                    elapsed,
//                    root.path("usage").path("total_tokens").asInt());
//
//            return content;
//
//        } catch (Exception e) {
//            log.error("OpenAI API call failed: {}", e.getMessage());
//            throw new RuntimeException("AI service temporarily unavailable: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Convenience method — single user prompt with no system context.
//     */
//    public String complete(String userPrompt) {
//        return complete("You are a helpful medical education assistant.", userPrompt);
//    }
//}