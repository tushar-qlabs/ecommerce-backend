package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddItemToWishlistRequestDTO(
        @NotNull(message = "Product Variant ID is required")
        Long productVariantId
) {}