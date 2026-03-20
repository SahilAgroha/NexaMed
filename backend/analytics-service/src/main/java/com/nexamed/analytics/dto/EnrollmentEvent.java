package com.nexamed.analytics.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnrollmentEvent {
    private String        eventType;
    private UUID          studentId;
    private String        studentEmail;
    private UUID          courseId;
    private String        courseTitle;
    private String        courseDifficulty;
    private LocalDateTime enrolledAt;
}