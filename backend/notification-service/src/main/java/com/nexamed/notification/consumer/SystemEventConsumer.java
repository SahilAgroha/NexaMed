package com.nexamed.notification.consumer;

import com.nexamed.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumes generic system events — quiz submitted, interview complete, etc.
 * Uses String deserialization since these events come in various formats.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SystemEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${kafka.topics.quiz-submitted}",
            groupId = "notification-service-group"
    )
    public void handleQuizSubmitted(@Payload String eventJson) {
        log.info("Quiz submitted event received: {}", eventJson);
        // Parse and push notification — will be wired to AI service results later
    }

    @KafkaListener(
            topics = "${kafka.topics.course-completed}",
            groupId = "notification-service-group"
    )
    public void handleCourseCompleted(@Payload String eventJson) {
        log.info("Course completed event received: {}", eventJson);
        // Push congratulations notification to student
    }
}