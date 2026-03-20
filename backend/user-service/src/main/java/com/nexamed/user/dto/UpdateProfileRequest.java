package com.nexamed.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 100)
    private String fullName;

    @Size(max = 500)
    private String bio;

    private String avatarUrl;

    @Size(max = 100)
    private String specialization;

    @Size(max = 200)
    private String institution;
}