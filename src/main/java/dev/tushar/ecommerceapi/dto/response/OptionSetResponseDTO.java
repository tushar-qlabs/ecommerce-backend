package dev.tushar.ecommerceapi.dto.response;

import java.util.List;

public record OptionSetResponseDTO(
        Long id,
        String name,
        List<String> options
) {}