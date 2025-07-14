package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AttributeRequestDTO(
        @NotBlank(message = "Attribute name is required")
        @Size(min = 1, max = 100, message = "Attribute name must be between 1 and 100 characters")
        String name
) {}