package com.nexamed.course.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrollments", schema = "course_service",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Enrollment {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @ToString.Exclude
    private Course course;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Builder.Default
    private int progressPercent = 0;

    @CreationTimestamp
    private LocalDateTime enrolledAt;

    private LocalDateTime completedAt;
}