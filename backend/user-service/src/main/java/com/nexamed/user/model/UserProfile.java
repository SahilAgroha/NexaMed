package com.nexamed.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores extended profile info for a user.
 * userId links back to auth-service — NOT a foreign key (cross-service boundary).
 * Auth credentials (password, JWT) live in auth-service only.
 */
@Entity
@Table(name = "user_profiles", schema = "user_service")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;              // auth-service User.id

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    private String role;              // STUDENT / TEACHER / ADMIN / INTERVIEWER

    private String bio;

    private String avatarUrl;

    private String specialization;   // e.g. Cardiology, Pharmacology

    private String institution;      // university or hospital name

    @Builder.Default
    private boolean profileComplete = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}