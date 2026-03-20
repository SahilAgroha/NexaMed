package com.nexamed.course.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ModuleRequest {
    @NotBlank private String title;
    private String content;
    private String videoUrl;
    private int orderIndex;
}