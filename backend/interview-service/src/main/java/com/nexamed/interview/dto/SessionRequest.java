package com.nexamed.interview.dto;

import com.nexamed.interview.model.InterviewType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionRequest {
    @NotBlank
    private String specialty;
    private InterviewType type = InterviewType.AI_MOCK;
    private LocalDateTime scheduledAt;
    private String interviewerId;    // only for LIVE type
}