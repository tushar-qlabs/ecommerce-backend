package dev.tushar.ecommerceapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponseDTO(
        Long id,
        String name,
        String description,
        BusinessInfo business,
        CategoryInfo category,
        List<ProductVariantDTO> variants
) {

    public record BusinessInfo(
            Long id,
            String name
    ) {}

    public record CategoryInfo(
            Long id,
            String name
    ) {}

    public record ProductVariantDTO(
            Long id,
            BigDecimal price,
            int stockQuantity,
            String sku, // We don't really need it for now!
            Map<String, String> attributes, // e.g., {"color": "Red", "size": "M"}
            List<String> image_urls
    ) {}
}
