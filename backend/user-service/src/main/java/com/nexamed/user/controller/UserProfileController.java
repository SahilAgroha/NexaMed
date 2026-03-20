package com.nexamed.user.controller;

import com.nexamed.user.dto.CreateProfileRequest;
import com.nexamed.user.dto.UpdateProfileRequest;
import com.nexamed.user.dto.UserProfileDto;
import com.nexamed.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    /**
     * GET /api/users/me
     * Returns the calling user's own profile.
     * X-User-Id is injected by the API Gateway's AuthFilter.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    /**
     * PUT /api/users/me
     * Update the calling user's own profile.
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateMyProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }

    /**
     * GET /api/users/{id}
     * Public profile view by profile UUID.
     * Used by teachers viewing student profiles, etc.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDto> getProfileById(@PathVariable UUID id) {
        return ResponseEntity.ok(profileService.getProfileById(id));
    }

    /**
     * POST /api/users/internal/create
     * Called by auth-service (via Feign) after a new user registers.
     * Creates a matching profile record in user-service DB.
     *
     * Not exposed through API Gateway to end users —
     * only internal service-to-service calls reach this.
     */
    @PostMapping("/internal/create")
    public ResponseEntity<UserProfileDto> createProfile(
            @Valid @RequestBody CreateProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createProfile(request));
    }

    /**
     * GET /api/users/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User service is running");
    }
}