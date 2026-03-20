package com.nexamed.course.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Publishes enrollment events to Kafka.
 * notification-service and analytics-service listen on this topic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollmentEventPublisher {

    private final KafkaTemplate<String, EnrollmentEvent> kafkaTemplate;

    @Value("${kafka.topics.enrollment-created}")
    private String enrollmentTopic;

    public void publish(EnrollmentEvent event) {
        // Key = studentId — ensures all events for same student go to same partition
        CompletableFuture<SendResult<String, EnrollmentEvent>> future =
                kafkaTemplate.send(enrollmentTopic, event.getStudentId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish enrollment event for studentId {}: {}",
                        event.getStudentId(), ex.getMessage());
            } else {
                log.info("Enrollment event published → topic: {}, partition: {}, offset: {}",
                        enrollmentTopic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}