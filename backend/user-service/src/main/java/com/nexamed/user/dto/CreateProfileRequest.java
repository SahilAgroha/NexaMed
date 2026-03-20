package com.nexamed.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

// Called internally when a new user registers in auth-service
@Data
public class CreateProfileRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    private String fullName;

    @Email @NotBlank
    private String email;

    private String role = "STUDENT";
}