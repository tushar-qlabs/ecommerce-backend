package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.constraints.Size;

public record BusinessUpdateRequestDTO(
        @Size(min = 3, max = 100, message = "Business name must be between 3 and 100 characters")
        String businessName,

        @Size(min = 3, max = 1000, message = "Business description must be between 3 and 1000 characters")
        String businessDescription
) {}
