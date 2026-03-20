package com.nexamed.analytics.controller;

import com.nexamed.analytics.dto.StudentDashboardResponse;
import com.nexamed.analytics.model.ActivityRecord;
import com.nexamed.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/analytics/dashboard
     * Returns the student's complete dashboard stats.
     * X-User-Id injected by API Gateway.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardResponse> getDashboard(
            @RequestHeader("X-User-Id") String studentId) {
        return ResponseEntity.ok(analyticsService.getDashboard(studentId));
    }

    /**
     * GET /api/analytics/timeline
     * Returns full activity history (enrollments, interviews, quizzes).
     */
    @GetMapping("/timeline")
    public ResponseEntity<List<ActivityRecord>> getTimeline(
            @RequestHeader("X-User-Id") String studentId) {
        return ResponseEntity.ok(analyticsService.getActivityTimeline(studentId));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "Analytics service is running"));
    }
}