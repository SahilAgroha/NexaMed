package com.nexamed.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String role;
    private String email;
    private String fullName;
    private String userId;
}