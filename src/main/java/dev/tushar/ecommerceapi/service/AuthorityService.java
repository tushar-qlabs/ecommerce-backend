package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.RoleRequestDTO;
import dev.tushar.ecommerceapi.dto.request.UpdateUserPermissionsRequestDTO;
import dev.tushar.ecommerceapi.dto.request.UpdateUserRolesRequestDTO;
import dev.tushar.ecommerceapi.dto.response.PermissionResponseDTO;
import dev.tushar.ecommerceapi.dto.response.RoleResponseDTO;
import dev.tushar.ecommerceapi.dto.response.UserAuthorityDetailsResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import java.util.List;

public interface AuthorityService {
    ApiResponse<List<RoleResponseDTO>> getAllRoles();
    ApiResponse<RoleResponseDTO> getRoleById(Long roleId);
    ApiResponse<RoleResponseDTO> createRole(RoleRequestDTO roleRequest);
    ApiResponse<RoleResponseDTO> updateRole(Long roleId, RoleRequestDTO roleRequest);
    ApiResponse<Void> deleteRole(Long roleId);

    ApiResponse<List<PermissionResponseDTO>> getAllPermissions();
    ApiResponse<PermissionResponseDTO> getPermissionById(Long permissionId);

    ApiResponse<Void> updateUserRoles(Long userId, UpdateUserRolesRequestDTO request);
    ApiResponse<Void> updateUserPermissions(Long userId, UpdateUserPermissionsRequestDTO request);

    // Updated to return the new, detailed DTO
    ApiResponse<UserAuthorityDetailsResponseDTO> getMyAuthorities(CustomUserDetails currentUser);
    ApiResponse<UserAuthorityDetailsResponseDTO> getUserAuthorities(Long userId);
}