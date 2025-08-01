package dev.tushar.ecommerceapi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        Long orderId,
        String status,
        LocalDateTime createdAt,
        BigDecimal grandTotal,
        AddressResponseDTO shippingAddress,
        List<OrderItemDTO> items
) {

    public record OrderItemDTO(
            Integer quantity,
            BigDecimal priceAtPurchase,
            ProductResponseDTO product,
            BusinessInfoDTO seller
    ) {}

    public record BusinessInfoDTO(
            String businessName
    ) {}
}