package com.nexamed.course.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "modules", schema = "course_service")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Module {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String videoUrl;
    private int orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @ToString.Exclude
    private Course course;
}