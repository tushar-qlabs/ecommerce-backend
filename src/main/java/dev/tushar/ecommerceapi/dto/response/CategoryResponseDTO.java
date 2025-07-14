package dev.tushar.ecommerceapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Set;

public record CategoryResponseDTO(
        Long id,
        String name,
        Long parentId,
        List<AncestorDTO> breadcrumb,
        Set<CategoryAttributeResponseDTO> attributes,
        Set<CategoryResponseDTO> subCategories
) {
    public record AncestorDTO(Long id, String name) {}
}