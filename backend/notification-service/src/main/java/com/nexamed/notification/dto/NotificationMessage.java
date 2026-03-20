package com.nexamed.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Sent to the browser via WebSocket STOMP.
 * Frontend subscribes to /topic/notifications/{userId}
 */
@Data
@Builder
public class NotificationMessage {
    private String        type;        // ENROLLMENT, QUIZ_RESULT, SYSTEM, etc.
    private String        title;
    private String        message;
    private String        userId;
    private LocalDateTime timestamp;
    private Object        payload;     // optional extra data
}