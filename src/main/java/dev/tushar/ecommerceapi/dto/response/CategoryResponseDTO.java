package dev.tushar.ecommerceapi.dto.response;

import java.util.Set;

public record CategoryResponseDTO(
        Long id,
        String name,
        Long parentId,
        Set<CategoryResponseDTO> subCategories
) {}
