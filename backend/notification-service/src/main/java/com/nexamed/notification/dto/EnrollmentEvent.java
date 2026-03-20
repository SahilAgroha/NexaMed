package com.nexamed.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mirror of course-service's EnrollmentEvent.
 * @JsonIgnoreProperties — safe to receive even if course-service adds new fields later.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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