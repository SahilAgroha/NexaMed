package com.nexamed.course.dto;

import com.nexamed.course.model.Category;
import com.nexamed.course.model.Difficulty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseRequest {
    @NotBlank private String title;
    private String description;
    private Difficulty difficulty;
    private Category category;
    private String thumbnailUrl;
}