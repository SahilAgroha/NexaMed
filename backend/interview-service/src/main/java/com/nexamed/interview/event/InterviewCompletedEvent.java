package com.nexamed.interview.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Published to Kafka when an interview session completes.
 * Consumed by: analytics-service, notification-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewCompletedEvent {
    private String        eventType;     // "INTERVIEW_COMPLETED"
    private UUID          sessionId;
    private UUID          studentId;
    private String        interviewType; // AI_MOCK or LIVE
    private String        specialty;
    private Integer       overallScore;
    private LocalDateTime completedAt;
}