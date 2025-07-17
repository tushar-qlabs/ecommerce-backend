package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}