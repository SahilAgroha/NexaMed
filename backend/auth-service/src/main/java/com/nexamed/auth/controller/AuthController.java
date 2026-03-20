package com.nexamed.auth.controller;

import com.nexamed.auth.dto.AuthResponse;
import com.nexamed.auth.dto.LoginRequest;
import com.nexamed.auth.dto.RefreshTokenRequest;
import com.nexamed.auth.dto.RegisterRequest;
import com.nexamed.auth.service.AuthService;
import com.nexamed.auth.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    /**
     * POST /api/auth/register
     * PUBLIC — no token needed
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * POST /api/auth/login
     * PUBLIC — no token needed
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /api/auth/refresh
     * PUBLIC — uses refresh token, not access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    /**
     * POST /api/auth/logout
     * PROTECTED — requires Authorization: Bearer <token>
     * Blacklists the token in Redis
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * GET /api/auth/me
     * PROTECTED — must be called through API Gateway (port 8080), NOT directly (port 8081)
     *
     * When called via gateway:
     *   Gateway validates JWT → injects X-User-Id, X-User-Role, X-User-Email headers
     *   This endpoint just reads those headers
     *
     * When called directly (bypassing gateway):
     *   Headers won't exist → we parse the JWT ourselves as fallback
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(
            @RequestHeader(value = "X-User-Id",    required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestHeader(value = "X-User-Role",  required = false) String role,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // ── Path 1: called through API Gateway (normal production path) ──
        if (userId != null && email != null && role != null) {
            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "email",  email,
                    "role",   role,
                    "source", "gateway-headers"
            ));
        }

        // ── Path 2: called directly (testing/dev only) ──────────────────
        // Parse JWT ourselves since gateway didn't inject headers
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token    = authHeader.substring(7);
                String emailVal = jwtService.extractUsername(token);
                Map<String, Object> claims = jwtService.extractAllClaims(token);

                return ResponseEntity.ok(Map.of(
                        "userId", claims.getOrDefault("userId", "unknown").toString(),
                        "email",  emailVal,
                        "role",   claims.getOrDefault("role", "unknown").toString(),
                        "source", "direct-jwt-parse"
                ));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "No authentication provided"));
    }

    /**
     * GET /api/auth/health
     * PUBLIC — quick sanity check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "Auth service is running"));
    }
}