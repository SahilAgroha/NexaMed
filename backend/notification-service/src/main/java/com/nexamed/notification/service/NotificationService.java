package com.nexamed.notification.service;

import com.nexamed.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Pushes notifications to connected browser clients via WebSocket STOMP.
 *
 * Each user subscribes to their own topic:
 *   /topic/notifications/{userId}
 *
 * The frontend receives messages instantly when:
 *   - They enroll in a course
 *   - A quiz result is ready
 *   - An interview is scheduled
 *   - Any system event happens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Push notification to a specific user.
     * User must be subscribed to /topic/notifications/{userId}
     */
    public void pushToUser(String userId, String type, String title, String message) {
        pushToUser(userId, type, title, message, null);
    }

    public void pushToUser(String userId, String type, String title, String message, Object payload) {
        NotificationMessage notification = NotificationMessage.builder()
                .type(type)
                .title(title)
                .message(message)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .payload(payload)
                .build();

        String destination = "/topic/notifications/" + userId;
        messagingTemplate.convertAndSend(destination, notification);

        log.info("Notification pushed → userId: {}, type: {}, title: {}",
                userId, type, title);
    }

    /**
     * Broadcast to ALL connected users.
     * Used for system announcements, maintenance notices, etc.
     */
    public void broadcast(String type, String title, String message) {
        NotificationMessage notification = NotificationMessage.builder()
                .type(type)
                .title(title)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/notifications/all", notification);
        log.info("Broadcast notification → type: {}, title: {}", type, title);
    }
}