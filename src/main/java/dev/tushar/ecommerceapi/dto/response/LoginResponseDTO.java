package dev.tushar.ecommerceapi.dto.response;

import java.util.HashMap;

public record LoginResponseDTO(
        Long id,
        String firstName,
        String email,
        HashMap<String, String> jwt
) {}