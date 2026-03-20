package com.nexamed.course.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handles @Valid failures — returns which fields failed and why
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field   = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", 400,
                        "error",  "Validation failed",
                        "fields", fieldErrors
                ));
    }

    // Handles business logic errors — "Course not found", "Already enrolled", etc.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        log.error("Runtime error: {}", ex.getMessage());

        // Return 404 for "not found" errors, 400 for everything else
        HttpStatus status = ex.getMessage() != null && ex.getMessage().contains("not found")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity
                .status(status)
                .body(Map.of(
                        "status", status.value(),
                        "error",  ex.getMessage()
                ));
    }

    // Catch-all for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "status", 500,
                        "error",  "Internal server error"
                ));
    }
}