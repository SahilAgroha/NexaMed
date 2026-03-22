package com.nexamed.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexamed.ai.client.GeminiClient;
//import com.nexamed.ai.client.OpenAIClient;
import com.nexamed.ai.dto.EvalRequest;
import com.nexamed.ai.dto.EvalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewEvalService {

//    private final OpenAIClient openAIClient;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;


    public EvalResponse evaluateAnswer(EvalRequest request) {

        String systemPrompt = """
                You are a senior medical examiner evaluating a candidate's interview answer.
                Be constructive, specific, and medically accurate.
                Return ONLY valid JSON.
                """;

        String userPrompt = """
                Evaluate this medical interview answer:
                
                QUESTION: %s
                
                EXPECTED TOPICS TO COVER: %s
                
                CANDIDATE'S ANSWER: %s
                
                SPECIALTY CONTEXT: %s
                
                Return ONLY this JSON:
                {
                  "overallScore": 85,
                  "clarityScore": 80,
                  "accuracyScore": 90,
                  "completenessScore": 80,
                  "strengths": ["Strength 1", "Strength 2"],
                  "improvements": ["Area to improve 1", "Area to improve 2"],
                  "detailedFeedback": "2-3 sentence detailed feedback",
                  "modelAnswer": "What an ideal answer would include in 3-4 sentences"
                }
                
                Scoring guide:
                - overallScore: weighted average of all scores
                - clarityScore: how well-structured and clear the answer was
                - accuracyScore: medical accuracy of the content
                - completenessScore: how many expected topics were covered
                """.formatted(
                request.getQuestion(),
                request.getExpectedTopics() != null ? request.getExpectedTopics() : "General medical knowledge",
                request.getAnswer(),
                request.getSpecialty() != null ? request.getSpecialty() : "General Medicine"
        );

        String raw = geminiClient.generate(systemPrompt, userPrompt);
        return parseEval(raw);
    }

    private EvalResponse parseEval(String json) {
        try {
            String clean = json.trim()
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            JsonNode node = objectMapper.readTree(clean);

            return EvalResponse.builder()
                    .overallScore(node.path("overallScore").asInt())
                    .clarityScore(node.path("clarityScore").asInt())
                    .accuracyScore(node.path("accuracyScore").asInt())
                    .completenessScore(node.path("completenessScore").asInt())
                    .strengths(parseStringList(node.path("strengths")))
                    .improvements(parseStringList(node.path("improvements")))
                    .detailedFeedback(node.path("detailedFeedback").asText())
                    .modelAnswer(node.path("modelAnswer").asText())
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse eval JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to evaluate answer. Please try again.");
        }
    }

    private List<String> parseStringList(JsonNode node) {
        List<String> result = new ArrayList<>();
        if (node.isArray()) node.forEach(n -> result.add(n.asText()));
        return result;
    }
}