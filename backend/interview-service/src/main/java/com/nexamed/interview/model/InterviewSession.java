package com.nexamed.interview.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "interview_sessions", schema = "interview_service")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InterviewSession {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID studentId;

    private UUID interviewerId;       // null for AI mock sessions

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InterviewType type = InterviewType.AI_MOCK;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SessionStatus status = SessionStatus.SCHEDULED;

    private String specialty;
    private String roomId;            // WebRTC room identifier

    private Integer overallScore;
    private Integer clarityScore;
    private Integer accuracyScore;

    @Column(columnDefinition = "TEXT")
    private String feedbackSummary;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default @ToString.Exclude
    private List<InterviewQuestion> questions = new ArrayList<>();

    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @CreationTimestamp private LocalDateTime createdAt;
    @UpdateTimestamp   private LocalDateTime updatedAt;
}