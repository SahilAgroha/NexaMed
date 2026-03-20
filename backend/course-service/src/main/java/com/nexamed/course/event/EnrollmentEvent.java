package com.nexamed.course.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Published to Kafka topic: enrollment.created
 * Consumed by: notification-service, analytics-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentEvent {
    private String eventType;       // "ENROLLMENT_CREATED"
    private UUID   studentId;
    private String studentEmail;
    private UUID   courseId;
    private String courseTitle;
    private String courseDifficulty;
    private LocalDateTime enrolledAt;
}