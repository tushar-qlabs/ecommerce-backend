package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UpdateUserPermissionsRequestDTO(
        @NotEmpty(message = "At least one permission ID is required")
        Set<Integer> permissionIds
) {}