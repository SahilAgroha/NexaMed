package com.nexamed.analytics.consumer;

import com.nexamed.analytics.dto.InterviewCompletedEvent;
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
public class InterviewAnalyticsConsumer {

    private final AnalyticsService analyticsService;

    @KafkaListener(
            topics = "${kafka.topics.interview-completed}",
            groupId = "analytics-interview-group",
            containerFactory = "interviewKafkaListenerContainerFactory"
    )
    public void consume(
            @Payload InterviewCompletedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Analytics ← interview event: student={}, score={}, offset={}",
                event.getStudentId(), event.getOverallScore(), offset);

        analyticsService.handleInterviewCompleted(event);
    }
}