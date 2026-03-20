package com.nexamed.course.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "courses", schema = "course_service")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Course {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Difficulty difficulty = Difficulty.BEGINNER;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String thumbnailUrl;

    @Column(nullable = false)
    private UUID teacherId;

    @Builder.Default
    private boolean published = false;

    @Builder.Default
    private int enrollmentCount = 0;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default @ToString.Exclude
    private List<Module> modules = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt=LocalDateTime.now();
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}