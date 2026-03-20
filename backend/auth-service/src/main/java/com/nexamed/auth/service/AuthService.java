package com.nexamed.auth.service;

import com.nexamed.auth.client.UserServiceClient;
import com.nexamed.auth.dto.AuthResponse;
import com.nexamed.auth.dto.LoginRequest;
import com.nexamed.auth.dto.RegisterRequest;
import com.nexamed.auth.model.AuthProvider;
import com.nexamed.auth.model.Role;
import com.nexamed.auth.model.User;
import com.nexamed.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final JwtService           jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserServiceClient    userServiceClient;   // Feign → user-service

    // ── Register ──────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already registered: " + request.getEmail());
        }

        Role role = parseRole(request.getRole());

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .provider(AuthProvider.LOCAL)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered: {} [{}]", saved.getEmail(), saved.getRole());

        // Auto-create matching profile in user-service via Feign
        // Fallback handles failure gracefully — registration still succeeds
        try {
            userServiceClient.createProfile(Map.of(
                    "userId",   saved.getId().toString(),
                    "fullName", saved.getFullName(),
                    "email",    saved.getEmail(),
                    "role",     saved.getRole().name()
            ));
            log.info("Profile created in user-service for: {}", saved.getEmail());
        } catch (Exception e) {
            // Non-fatal — profile will be created on next login or manually
            log.warn("Could not create profile in user-service: {}", e.getMessage());
        }

        return buildAuthResponse(saved);
    }

    // ── Login ─────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    // ── Refresh token ─────────────────────────────────────────────

    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new IllegalStateException("Invalid or expired refresh token");
        }
        return buildAuthResponse(user);
    }

    // ── Logout ────────────────────────────────────────────────────

    public void logout(String token) {
        jwtService.blacklistToken(token);
        log.info("Token blacklisted");
    }

    // ── Helpers ───────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtService.generateAccessToken(
                user, user.getRole().name(), user.getId().toString());
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .role(user.getRole().name())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .userId(user.getId().toString())
                .build();
    }

    private Role parseRole(String roleStr) {
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Role.STUDENT;
        }
    }
}