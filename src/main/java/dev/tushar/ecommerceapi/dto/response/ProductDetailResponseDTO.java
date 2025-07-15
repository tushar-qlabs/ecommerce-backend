package dev.tushar.ecommerceapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductDetailResponseDTO(
        Long productId,
        String name,
        String description,
        ProductResponseDTO.BusinessInfo business,
        ProductResponseDTO.CategoryInfo category,
        List<VariantDetailDTO> variants
) {
    public record VariantDetailDTO(
            Long variantId,
            BigDecimal price,
            int stockQuantity,
            Map<String, String> attributes
    ) {}
}