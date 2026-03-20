package com.nexamed.interview.dto;

import com.nexamed.interview.model.InterviewType;
import com.nexamed.interview.model.SessionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class SessionResponse {
    private UUID          id;
    private UUID          studentId;
    private InterviewType type;
    private SessionStatus status;
    private String        specialty;
    private String        roomId;
    private Integer       overallScore;
    private Integer       clarityScore;
    private Integer       accuracyScore;
    private String        feedbackSummary;
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}