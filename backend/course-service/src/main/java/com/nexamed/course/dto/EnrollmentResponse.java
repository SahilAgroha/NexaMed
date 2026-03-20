package com.nexamed.course.dto;

import com.nexamed.course.model.EnrollmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class EnrollmentResponse {
    private UUID id;
    private UUID studentId;
    private UUID courseId;
    private String courseTitle;
    private EnrollmentStatus status;
    private int progressPercent;
    private LocalDateTime enrolledAt;
}