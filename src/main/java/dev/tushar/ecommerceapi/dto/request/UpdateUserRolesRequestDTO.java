package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UpdateUserRolesRequestDTO(
        @NotEmpty(message = "At least one role ID is required")
        Set<Integer> roleIds
) {}