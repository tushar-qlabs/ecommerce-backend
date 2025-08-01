package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequestDTO(
        @NotNull(message = "Shipping address ID is required")
        Long shippingAddressId
) {}