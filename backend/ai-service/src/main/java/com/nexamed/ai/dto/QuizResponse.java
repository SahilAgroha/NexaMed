package com.nexamed.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuizResponse {
    private String topic;
    private String difficulty;
    private int questionCount;
    private List<QuizQuestion> questions;
    private long generatedInMs;  // response time tracking
}