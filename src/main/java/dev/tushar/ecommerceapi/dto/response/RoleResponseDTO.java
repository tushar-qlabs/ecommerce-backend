package dev.tushar.ecommerceapi.dto.response;

import java.util.Set;

public record RoleResponseDTO(
        Integer id,
        String name,
        Set<PermissionResponseDTO> permissions
) {}