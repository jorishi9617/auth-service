package com.videoplatform.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {}

    public record Credentials(
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank String password) {}

    public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds, UUID userId, String email) {}
}
