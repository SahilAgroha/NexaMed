package com.nexamed.notification.controller;

import com.nexamed.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification service is running");
    }

    /** Manually push a test notification — useful during frontend dev */
    @PostMapping("/test/{userId}")
    public ResponseEntity<Map<String, String>> sendTest(@PathVariable String userId) {
        notificationService.pushToUser(userId, "TEST", "Test Notification",
                "This is a test notification from NexaMed");
        return ResponseEntity.ok(Map.of("message", "Test notification sent to: " + userId));
    }

    /** Broadcast to all connected users */
    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, String>> broadcast(@RequestBody Map<String, String> req) {
        notificationService.broadcast(
                req.getOrDefault("type", "SYSTEM"),
                req.getOrDefault("title", "System Notification"),
                req.getOrDefault("message", ""));
        return ResponseEntity.ok(Map.of("message", "Broadcast sent"));
    }
}