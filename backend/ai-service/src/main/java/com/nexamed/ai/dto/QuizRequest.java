package com.nexamed.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuizRequest {

    @NotBlank(message = "Topic is required")
    private String topic;               // e.g. "Pharmacokinetics", "Cardiac anatomy"

    private String difficulty = "INTERMEDIATE";  // BEGINNER, INTERMEDIATE, ADVANCED

    @Min(1) @Max(20)
    private int questionCount = 5;

    private String courseContext;       // optional: extra context from the course
}