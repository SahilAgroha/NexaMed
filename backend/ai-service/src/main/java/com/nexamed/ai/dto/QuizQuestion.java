package com.nexamed.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion {
    private int    questionNumber;
    private String question;
    private List<String> options;      // always 4 options (A, B, C, D)
    private int    correctAnswerIndex; // 0-3
    private String explanation;        // why the answer is correct
    private String difficulty;
}