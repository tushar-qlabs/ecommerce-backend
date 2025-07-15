package dev.tushar.ecommerceapi.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponseDTO(
        Long cartId,
        Long userId,
        List<CartItemDetailDTO> items,
        BigDecimal subtotal
) {
    public record CartItemDetailDTO(
            Long cartItemId,
            Long quantity,
            ProductResponseDTO product
    ) {}
}