package com.nexamed.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EvalRequest {

    @NotBlank
    private String question;           // interview question asked

    @NotBlank
    private String answer;             // student's transcribed answer

    private String expectedTopics;     // key concepts the answer should cover
    private String specialty;          // medical specialty context
}