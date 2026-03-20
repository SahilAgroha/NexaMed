package com.nexamed.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserProfileDto {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String email;
    private String role;
    private String bio;
    private String avatarUrl;
    private String specialization;
    private String institution;
    private boolean profileComplete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}