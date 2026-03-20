package com.nexamed.analytics.consumer;

import com.nexamed.analytics.dto.EnrollmentEvent;
import com.nexamed.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollmentAnalyticsConsumer {

    private final AnalyticsService analyticsService;

    @KafkaListener(
            topics = "${kafka.topics.enrollment-created}",
            groupId = "analytics-enrollment-group",
            containerFactory = "enrollmentKafkaListenerContainerFactory"
    )
    public void consume(
            @Payload EnrollmentEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Analytics ← enrollment event: student={}, course='{}', offset={}",
                event.getStudentId(), event.getCourseTitle(), offset);

        analyticsService.handleEnrollment(event);
    }
}