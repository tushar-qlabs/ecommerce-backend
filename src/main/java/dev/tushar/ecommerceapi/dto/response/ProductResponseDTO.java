package dev.tushar.ecommerceapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponseDTO(
        Long productId,
        String name,
        String description,
        BusinessInfo business,
        CategoryInfo category,

        Long variantId,
        BigDecimal price,
        int stockQuantity,
        Map<String, String> attributes,
        List<String> imageUrls
) {
    public record BusinessInfo(Long id, String name) {}
    public record CategoryInfo(Long id, String name) {}
}