package com.nexamed.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class SubmitAnswerRequest {
    @NotBlank private String answer;
    private UUID questionId;        // which question was answered
}