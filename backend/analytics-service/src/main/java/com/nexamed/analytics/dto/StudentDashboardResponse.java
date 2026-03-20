package com.nexamed.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class StudentDashboardResponse {
    private UUID   studentId;

    // Summary cards
    private int    totalEnrollments;
    private int    completedCourses;
    private int    totalInterviews;
    private int    averageInterviewScore;
    private int    bestInterviewScore;
    private int    totalQuizzesTaken;
    private int    averageQuizScore;
    private int    streakDays;

    // Recent activity timeline
    private List<ActivitySummary> recentActivity;

    private LocalDateTime lastActiveAt;
    private LocalDateTime generatedAt;

    @Data @Builder
    public static class ActivitySummary {
        private String        type;
        private String        description;
        private Integer       score;
        private LocalDateTime occurredAt;
    }
}