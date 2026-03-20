package com.nexamed.course.dto;

import com.nexamed.course.model.Category;
import com.nexamed.course.model.Difficulty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class CourseResponse {
    private UUID id;
    private String title;
    private String description;
    private Difficulty difficulty;
    private Category category;
    private String thumbnailUrl;
    private UUID teacherId;
    private boolean published;
    private int enrollmentCount;
    private int moduleCount;
    private LocalDateTime createdAt;
}