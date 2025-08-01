package dev.tushar.ecommerceapi.dto.request;

public record AddItemToCartRequestDTO(Long userId, Long productVariantId, Integer quantity) {}