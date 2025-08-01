package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ValidateBusinessRequestDTO(
        @NotBlank(message = "Validation status is required")
        String validationStatus // VERIFIED, REJECTED, PENDING
) {}
