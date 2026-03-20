package com.nexamed.analytics.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nexamed.analytics.dto.EnrollmentEvent;
import com.nexamed.analytics.dto.InterviewCompletedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    private Map<String, Object> baseConsumerProps(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    // ── Enrollment event consumer factory ─────────────────────────

    @Bean
    public ConsumerFactory<String, EnrollmentEvent> enrollmentConsumerFactory() {
        JsonDeserializer<EnrollmentEvent> deser = new JsonDeserializer<>(EnrollmentEvent.class, objectMapper());
        deser.addTrustedPackages("*");
        deser.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(
                baseConsumerProps("analytics-enrollment-group"),
                new StringDeserializer(), deser);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EnrollmentEvent>
    enrollmentKafkaListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, EnrollmentEvent>();
        factory.setConsumerFactory(enrollmentConsumerFactory());
        return factory;
    }

    // ── Interview event consumer factory ──────────────────────────

    @Bean
    public ConsumerFactory<String, InterviewCompletedEvent> interviewConsumerFactory() {
        JsonDeserializer<InterviewCompletedEvent> deser =
                new JsonDeserializer<>(InterviewCompletedEvent.class, objectMapper());
        deser.addTrustedPackages("*");
        deser.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(
                baseConsumerProps("analytics-interview-group"),
                new StringDeserializer(), deser);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InterviewCompletedEvent>
    interviewKafkaListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, InterviewCompletedEvent>();
        factory.setConsumerFactory(interviewConsumerFactory());
        return factory;
    }
}