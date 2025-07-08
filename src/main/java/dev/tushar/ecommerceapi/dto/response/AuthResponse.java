package dev.tushar.ecommerceapi.dto.response;

import java.util.HashMap;

public record AuthResponse(
        Long id,
        String firstName,
        String email,
        String[] permissions,
        String[] roles,
        HashMap<String, String> jwt
) {}