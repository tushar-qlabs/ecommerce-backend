package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddItemToWishlistRequestDTO(
        @NotBlank(message = "Product Variant ID is required")
        Long productVariantId
) {}