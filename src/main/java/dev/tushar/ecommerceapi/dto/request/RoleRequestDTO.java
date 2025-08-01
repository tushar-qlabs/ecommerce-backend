package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class RoleRequestDTO {
    @NotBlank(message = "Role name is required")
    private String name;

    @NotEmpty(message = "At least one permission is required")
    private Set<Integer> permissionIds;
}