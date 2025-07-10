package dev.tushar.ecommerceapi.dto.response;

public record RegisterResponseDTO(
        Long id,
        String firstName,
        String lastName,
        String email
) {}

