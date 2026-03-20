package com.nexamed.user.service;

import com.nexamed.user.dto.CreateProfileRequest;
import com.nexamed.user.dto.UpdateProfileRequest;
import com.nexamed.user.dto.UserProfileDto;
import com.nexamed.user.model.UserProfile;
import com.nexamed.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository profileRepository;

    // ── Create profile (called right after auth-service registers user) ──

    @Transactional
    public UserProfileDto createProfile(CreateProfileRequest request) {
        if (profileRepository.existsByUserId(request.getUserId())) {
            // Idempotent — return existing profile if already created
            return profileRepository.findByUserId(request.getUserId())
                    .map(this::toDto)
                    .orElseThrow();
        }

        UserProfile profile = UserProfile.builder()
                .userId(request.getUserId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(request.getRole())
                .build();

        UserProfile saved = profileRepository.save(profile);
        log.info("Profile created for userId: {}", saved.getUserId());
        return toDto(saved);
    }

    // ── Get profile by userId (from X-User-Id gateway header) ────────

    public UserProfileDto getProfileByUserId(String userId) {
        return profileRepository.findByUserId(UUID.fromString(userId))
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));
    }

    // ── Get profile by profileId (public profile view) ────────────────

    public UserProfileDto getProfileById(UUID id) {
        return profileRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Profile not found: " + id));
    }

    // ── Update own profile ────────────────────────────────────────────

    @Transactional
    public UserProfileDto updateProfile(String userId, UpdateProfileRequest request) {
        UserProfile profile = profileRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));

        // Only update fields that are provided (non-null)
        if (request.getFullName()      != null) profile.setFullName(request.getFullName());
        if (request.getBio()           != null) profile.setBio(request.getBio());
        if (request.getAvatarUrl()     != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getSpecialization()!= null) profile.setSpecialization(request.getSpecialization());
        if (request.getInstitution()   != null) profile.setInstitution(request.getInstitution());

        // Mark complete if key fields filled in
        profile.setProfileComplete(
                profile.getBio() != null && profile.getInstitution() != null
        );

        return toDto(profileRepository.save(profile));
    }

    // ── Mapper ────────────────────────────────────────────────────────

    private UserProfileDto toDto(UserProfile p) {
        return UserProfileDto.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .fullName(p.getFullName())
                .email(p.getEmail())
                .role(p.getRole())
                .bio(p.getBio())
                .avatarUrl(p.getAvatarUrl())
                .specialization(p.getSpecialization())
                .institution(p.getInstitution())
                .profileComplete(p.isProfileComplete())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}