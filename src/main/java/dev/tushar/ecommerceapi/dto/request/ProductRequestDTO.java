package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductRequestDTO(
        @NotBlank(message = "Product name is required")
        String name,

        @NotBlank(message = "Product description is required")
        String description,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotEmpty(message = "At least one product variant is required")
        @Valid
        List<ProductVariantRequestDTO> variants
) {
        public record ProductVariantRequestDTO(
                @NotNull(message = "Price is required")
                @Positive(message = "Price must be positive")
                BigDecimal price,

                @NotNull(message = "Stock quantity is required")
                int stockQuantity,

                @NotEmpty(message = "Product categoryAttributes are required")
                Map<String, Object> attributes, // e.g., {"Size": "M", "Stretchable": true}

                List<String> imageUrls
        ) {}
}