package com.nexamed.interview.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Calls ai-service to evaluate a student's interview answer.
 * Uses lb://ai-service — Eureka resolves the actual host.
 */
@FeignClient(name = "ai-service")
public interface AiServiceClient {

    @PostMapping("/api/ai/eval/answer")
    Map<String, Object> evaluateAnswer(@RequestBody Map<String, Object> request);
}