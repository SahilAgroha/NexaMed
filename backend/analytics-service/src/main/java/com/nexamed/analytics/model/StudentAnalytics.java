package com.nexamed.analytics.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Aggregated analytics record per student.
 * Updated every time a Kafka event arrives for that student.
 * One row per student — upserted on each event.
 */
@Entity
@Table(name = "student_analytics", schema = "analytics_service")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StudentAnalytics {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID studentId;

    // Enrollment stats
    @Builder.Default
    private int totalEnrollments = 0;

    @Builder.Default
    private int completedCourses = 0;

    // Interview stats
    @Builder.Default
    private int totalInterviews = 0;

    @Builder.Default
    private int averageInterviewScore = 0;

    @Builder.Default
    private int bestInterviewScore = 0;

    // Quiz stats
    @Builder.Default
    private int totalQuizzesTaken = 0;

    @Builder.Default
    private int averageQuizScore = 0;

    // Engagement
    @Builder.Default
    private int streakDays = 0;     // consecutive days active

    private LocalDateTime lastActiveAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}