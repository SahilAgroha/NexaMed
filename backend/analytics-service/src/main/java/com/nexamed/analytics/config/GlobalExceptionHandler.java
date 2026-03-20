package com.nexamed.analytics.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        log.error("Analytics error: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("status", 400, "error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unhandled: {}", ex.getMessage(), ex);
        return ResponseEntity.status(500)
                .body(Map.of("status", 500, "error", "Internal server error"));
    }
}