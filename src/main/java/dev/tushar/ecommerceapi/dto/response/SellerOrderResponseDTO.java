package dev.tushar.ecommerceapi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SellerOrderResponseDTO(
        Long sellerOrderId,
        Long originalOrderId,
        LocalDateTime orderDate,
        String status,
        AddressResponseDTO shippingAddress,
        List<OrderItemDTO> items
) {
    public record OrderItemDTO(
            Long orderItemId,
            Integer quantity,
            BigDecimal priceAtPurchase,
            ProductResponseDTO product
    ) {}
}