package com.nexamed.analytics.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Individual activity log — one row per event.
 * Used for timeline views and trend charts.
 */
@Entity
@Table(name = "activity_records", schema = "analytics_service")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ActivityRecord {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    private String referenceId;     // courseId, sessionId, etc.
    private String referenceName;   // course title, specialty, etc.
    private Integer score;          // score if applicable

    @CreationTimestamp
    private LocalDateTime occurredAt;
}