package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePasswordRequestDTO(
        @NotBlank(message = "Old password is required")
        String oldPassword,

        @NotBlank(message = "New password is required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\s\\W]).{6,}$",
                message = "Password must be at least 6 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character"
        )
        String newPassword
) {}