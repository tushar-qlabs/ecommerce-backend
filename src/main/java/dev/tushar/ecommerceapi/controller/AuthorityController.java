package dev.tushar.ecommerceapi.controller;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.RoleRequestDTO;
import dev.tushar.ecommerceapi.dto.request.UpdateUserPermissionsRequestDTO;
import dev.tushar.ecommerceapi.dto.request.UpdateUserRolesRequestDTO;
import dev.tushar.ecommerceapi.dto.response.PermissionResponseDTO;
import dev.tushar.ecommerceapi.dto.response.RoleResponseDTO;
import dev.tushar.ecommerceapi.dto.response.UserAuthorityDetailsResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.AuthorityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/authorities")
@RequiredArgsConstructor
public class AuthorityController {

    private final AuthorityService authorityService;

    // --- Role Endpoints ---
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<List<RoleResponseDTO>>> getRoles() {
        List<RoleResponseDTO> roles = authorityService.getAllRoles();
        ApiResponse<List<RoleResponseDTO>> response = ApiResponse.success("Roles fetched successfully", roles, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> addRole(
            @Valid @RequestBody RoleRequestDTO request
    ) {
        RoleResponseDTO createdRole = authorityService.createRole(request);
        ApiResponse<RoleResponseDTO> response = ApiResponse.success("Role created successfully", createdRole, HttpStatus.CREATED.value());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> getRoleById(
            @PathVariable Long id
    ) {
        RoleResponseDTO role = authorityService.getRoleById(id);
        ApiResponse<RoleResponseDTO> response = ApiResponse.success("Role fetched successfully", role, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequestDTO request
    ) {
        RoleResponseDTO updatedRole = authorityService.updateRole(id, request);
        ApiResponse<RoleResponseDTO> response = ApiResponse.success("Role updated successfully", updatedRole, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @PathVariable Long id
    ) {
        authorityService.deleteRole(id);
        ApiResponse<Void> response = ApiResponse.success("Role deleted successfully", null, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    // --- Permission Endpoints ---
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<List<PermissionResponseDTO>>> getPermissions() {
        List<PermissionResponseDTO> permissions = authorityService.getAllPermissions();
        ApiResponse<List<PermissionResponseDTO>> response = ApiResponse.success("Permissions fetched successfully", permissions, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/permissions/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<PermissionResponseDTO>> getPermissionById(
            @PathVariable Long id
    ) {
        PermissionResponseDTO permission = authorityService.getPermissionById(id);
        ApiResponse<PermissionResponseDTO> response = ApiResponse.success("Permission fetched successfully", permission, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    // --- Assignment Endpoints ---
    @PutMapping("/roles/assign/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<Void>> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequestDTO request) {
        authorityService.updateUserRoles(userId, request);
        ApiResponse<Void> response = ApiResponse.success("User roles updated successfully", null, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/permissions/assign/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<Void>> updateUserPermissions(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserPermissionsRequestDTO request) {
        authorityService.updateUserPermissions(userId, request);
        ApiResponse<Void> response = ApiResponse.success("User permissions updated successfully", null, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    // --- User Authority Endpoints ---
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserAuthorityDetailsResponseDTO>> getMyAuthorities(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        UserAuthorityDetailsResponseDTO authorities = authorityService.getMyAuthorities(currentUser);
        ApiResponse<UserAuthorityDetailsResponseDTO> response = ApiResponse.success("Authorities fetched successfully", authorities, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<UserAuthorityDetailsResponseDTO>> getUserAuthorities(
            @PathVariable Long userId
    ) {
        UserAuthorityDetailsResponseDTO authorities = authorityService.getUserAuthorities(userId);
        ApiResponse<UserAuthorityDetailsResponseDTO> response = ApiResponse.success("Authorities for user fetched successfully", authorities, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}