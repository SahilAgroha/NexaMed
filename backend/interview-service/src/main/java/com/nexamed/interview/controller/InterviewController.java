package com.nexamed.interview.controller;

import com.nexamed.interview.dto.SessionRequest;
import com.nexamed.interview.dto.SessionResponse;
import com.nexamed.interview.dto.SubmitAnswerRequest;
import com.nexamed.interview.model.InterviewQuestion;
import com.nexamed.interview.service.MockInterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final MockInterviewService mockService;

    /** POST /api/interviews/mock/start — begin AI mock interview */
    @PostMapping("/mock/start")
    public ResponseEntity<SessionResponse> startMock(
            @Valid @RequestBody SessionRequest request,
            @RequestHeader("X-User-Id") String studentId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mockService.startSession(request, studentId));
    }

    /** POST /api/interviews/{id}/answer — submit an answer */
    @PostMapping("/{id}/answer")
    public ResponseEntity<Map<String, Object>> submitAnswer(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitAnswerRequest request,
            @RequestHeader("X-User-Id") String studentId) {
        return ResponseEntity.ok(mockService.submitAnswer(id, request, studentId));
    }

    /** POST /api/interviews/{id}/complete — end the session */
    @PostMapping("/{id}/complete")
    public ResponseEntity<SessionResponse> complete(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String studentId) {
        return ResponseEntity.ok(mockService.completeSession(id, studentId));
    }

    /** GET /api/interviews/history — student's past sessions */
    @GetMapping("/history")
    public ResponseEntity<List<SessionResponse>> getHistory(
            @RequestHeader("X-User-Id") String studentId) {
        return ResponseEntity.ok(mockService.getMyHistory(studentId));
    }

    /** GET /api/interviews/{id}/questions — all questions in a session */
    @GetMapping("/{id}/questions")
    public ResponseEntity<List<InterviewQuestion>> getQuestions(@PathVariable UUID id) {
        return ResponseEntity.ok(mockService.getSessionQuestions(id));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Interview service is running");
    }
}