package com.nexamed.ai.controller;

import com.nexamed.ai.dto.QuizRequest;
import com.nexamed.ai.dto.QuizResponse;
import com.nexamed.ai.service.QuizGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizGeneratorService quizService;

    /**
     * POST /api/ai/quiz/generate
     * Body: { topic, difficulty, questionCount, courseContext? }
     * Returns: list of MCQ questions with answers + explanations
     */
    @PostMapping("/generate")
    public ResponseEntity<QuizResponse> generateQuiz(
            @Valid @RequestBody QuizRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(quizService.generateQuiz(request));
    }
}