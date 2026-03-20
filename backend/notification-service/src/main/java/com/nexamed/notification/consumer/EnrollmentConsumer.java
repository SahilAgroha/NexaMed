package com.nexamed.notification.consumer;

import com.nexamed.notification.dto.EnrollmentEvent;
import com.nexamed.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listens on Kafka topic: enrollment.created
 * Published by: course-service when a student enrolls
 *
 * On receiving event:
 *  1. Logs the event details
 *  2. Pushes a WebSocket notification to the student's browser
 *
 * containerFactory = "enrollmentKafkaListenerContainerFactory"
 * This uses our custom KafkaConfig which handles LocalDateTime deserialization.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollmentConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${kafka.topics.enrollment-created}",
            groupId = "notification-service-group",
            containerFactory = "enrollmentKafkaListenerContainerFactory"
    )
    public void handleEnrollment(
            @Payload EnrollmentEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Kafka event received ← topic: {}, partition: {}, offset: {}",
                topic, partition, offset);
        log.info("Enrollment: studentId={}, course='{}'",
                event.getStudentId(), event.getCourseTitle());

        // Push real-time notification to the student's browser
        notificationService.pushToUser(
                event.getStudentId().toString(),
                "ENROLLMENT",
                "Enrollment Confirmed!",
                "You have successfully enrolled in \"" + event.getCourseTitle() + "\"",
                event  // attach full event as payload for frontend use
        );
    }
}