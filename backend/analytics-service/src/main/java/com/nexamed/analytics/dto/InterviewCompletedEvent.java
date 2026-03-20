package com.nexamed.analytics.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterviewCompletedEvent {
    private String        eventType;
    private UUID          sessionId;
    private UUID          studentId;
    private String        interviewType;
    private String        specialty;
    private Integer       overallScore;
    private LocalDateTime completedAt;
}