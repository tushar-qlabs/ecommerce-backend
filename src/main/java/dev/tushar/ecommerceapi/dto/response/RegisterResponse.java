package dev.tushar.ecommerceapi.dto.response;

public record RegisterResponse(
        Long id,
        String firstName,
        String lastName,
        String email
) {}

