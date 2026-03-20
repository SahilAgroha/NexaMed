package com.nexamed.ai.controller;

import com.nexamed.ai.dto.EvalRequest;
import com.nexamed.ai.dto.EvalResponse;
import com.nexamed.ai.service.InterviewEvalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/eval")
@RequiredArgsConstructor
public class InterviewEvalController {

    private final InterviewEvalService evalService;

    /**
     * POST /api/ai/eval/answer
     * Body: { question, answer, expectedTopics?, specialty? }
     * Returns: scores + feedback + model answer
     */
    @PostMapping("/answer")
    public ResponseEntity<EvalResponse> evaluateAnswer(
            @Valid @RequestBody EvalRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(evalService.evaluateAnswer(request));
    }
}