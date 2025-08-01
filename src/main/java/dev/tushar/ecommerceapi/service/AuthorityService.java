package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.request.RoleRequestDTO;
import dev.tushar.ecommerceapi.dto.request.UpdateUserPermissionsRequestDTO;
import dev.tushar.ecommerceapi.dto.request.UpdateUserRolesRequestDTO;
import dev.tushar.ecommerceapi.dto.response.PermissionResponseDTO;
import dev.tushar.ecommerceapi.dto.response.RoleResponseDTO;
import dev.tushar.ecommerceapi.dto.response.UserAuthorityDetailsResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import java.util.List;

public interface AuthorityService {
    List<RoleResponseDTO> getAllRoles();
    RoleResponseDTO getRoleById(Long roleId);
    RoleResponseDTO createRole(RoleRequestDTO roleRequest);
    RoleResponseDTO updateRole(Long roleId, RoleRequestDTO roleRequest);
    void deleteRole(Long roleId);

    List<PermissionResponseDTO> getAllPermissions();
    PermissionResponseDTO getPermissionById(Long permissionId);

    void updateUserRoles(Long userId, UpdateUserRolesRequestDTO request);
    void updateUserPermissions(Long userId, UpdateUserPermissionsRequestDTO request);

    UserAuthorityDetailsResponseDTO getMyAuthorities(CustomUserDetails currentUser);
    UserAuthorityDetailsResponseDTO getUserAuthorities(Long userId);
}