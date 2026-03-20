package com.nexamed.interview.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "interview_questions", schema = "interview_service")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InterviewQuestion {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    @ToString.Exclude
    private InterviewSession session;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(columnDefinition = "TEXT")
    private String studentAnswer;

    private Integer questionScore;

    @Column(columnDefinition = "TEXT")
    private String aiFeedback;

    private int questionOrder;
}