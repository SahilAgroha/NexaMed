package com.nexamed.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexamed.ai.client.OpenAIClient;
import com.nexamed.ai.dto.QuizQuestion;
import com.nexamed.ai.dto.QuizRequest;
import com.nexamed.ai.dto.QuizResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizGeneratorService {

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;

    public QuizResponse generateQuiz(QuizRequest request) {
        long start = System.currentTimeMillis();

        String systemPrompt = """
                You are a medical education expert creating high-quality MCQ questions.
                Always return valid JSON only. No markdown, no explanations outside the JSON.
                """;

        String userPrompt = """
                Generate %d multiple-choice questions about "%s" for medical students.
                Difficulty level: %s
                %s
                
                Return ONLY a JSON array. Each object must have exactly these fields:
                {
                  "questionNumber": 1,
                  "question": "Question text here",
                  "options": ["Option A", "Option B", "Option C", "Option D"],
                  "correctAnswerIndex": 0,
                  "explanation": "Explanation of why this answer is correct",
                  "difficulty": "%s"
                }
                
                Rules:
                - Questions must be clinically relevant and medically accurate
                - All 4 options must be plausible (no obviously wrong options)
                - Explanations must reference the underlying mechanism or concept
                - Return ONLY the JSON array, nothing else
                """.formatted(
                request.getQuestionCount(),
                request.getTopic(),
                request.getDifficulty(),
                request.getCourseContext() != null
                        ? "Additional context: " + request.getCourseContext()
                        : "",
                request.getDifficulty()
        );

        String rawResponse = openAIClient.complete(systemPrompt, userPrompt);
        List<QuizQuestion> questions = parseQuestions(rawResponse);

        return QuizResponse.builder()
                .topic(request.getTopic())
                .difficulty(request.getDifficulty())
                .questionCount(questions.size())
                .questions(questions)
                .generatedInMs(System.currentTimeMillis() - start)
                .build();
    }

    private List<QuizQuestion> parseQuestions(String json) {
        try {
            // Strip markdown code fences if GPT wraps in ```json ... ```
            String clean = json.trim()
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            return objectMapper.readValue(clean,
                    new TypeReference<List<QuizQuestion>>() {});

        } catch (Exception e) {
            log.error("Failed to parse quiz JSON: {}", e.getMessage());
            log.debug("Raw GPT response: {}", json);
            throw new RuntimeException("Failed to parse AI-generated questions. Please try again.");
        }
    }
}