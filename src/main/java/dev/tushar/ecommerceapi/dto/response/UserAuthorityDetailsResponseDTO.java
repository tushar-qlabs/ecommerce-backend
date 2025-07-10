package dev.tushar.ecommerceapi.dto.response;

import java.util.Set;

public record UserAuthorityDetailsResponseDTO(
        Set<RoleDetail> assignedRoles,
        Set<PermissionDetail> individualPermissions,
        Set<String> allEffectivePermissions
) {
    public record RoleDetail(Integer id, String name) {}
    public record PermissionDetail(Integer id, String name) {}
}