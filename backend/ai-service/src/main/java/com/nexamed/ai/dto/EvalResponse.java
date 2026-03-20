package com.nexamed.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EvalResponse {
    private int          overallScore;      // 0-100
    private int          clarityScore;      // 0-100
    private int          accuracyScore;     // 0-100
    private int          completenessScore; // 0-100
    private List<String> strengths;
    private List<String> improvements;
    private String       detailedFeedback;
    private String       modelAnswer;       // what a perfect answer looks like
}