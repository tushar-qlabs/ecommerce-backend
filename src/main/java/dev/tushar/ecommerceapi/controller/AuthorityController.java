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
        ApiResponse<List<RoleResponseDTO>> response = authorityService.getAllRoles();
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> addRole(@Valid @RequestBody RoleRequestDTO request) {
        ApiResponse<RoleResponseDTO> response = authorityService.createRole(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> getRoleById(@PathVariable Integer id) {
        ApiResponse<RoleResponseDTO> response = authorityService.getRoleById(id);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> updateRole(@PathVariable Integer id, @Valid @RequestBody RoleRequestDTO request) {
        ApiResponse<RoleResponseDTO> response = authorityService.updateRole(id, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Integer id) {
        ApiResponse<Void> response = authorityService.deleteRole(id);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    // --- Permission Endpoints ---
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<List<PermissionResponseDTO>>> getPermissions() {
        ApiResponse<List<PermissionResponseDTO>> response = authorityService.getAllPermissions();
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/permissions/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<PermissionResponseDTO>> getPermissionById(@PathVariable Integer id) {
        ApiResponse<PermissionResponseDTO> response = authorityService.getPermissionById(id);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    // --- Assignment Endpoints ---
    @PutMapping("/roles/assign/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<Void>> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequestDTO request) {
        ApiResponse<Void> response = authorityService.updateUserRoles(userId, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/permissions/assign/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<Void>> updateUserPermissions(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserPermissionsRequestDTO request) {
        ApiResponse<Void> response = authorityService.updateUserPermissions(userId, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    // --- User Authority Endpoints ---
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserAuthorityDetailsResponseDTO>> getMyAuthorities(@AuthenticationPrincipal CustomUserDetails currentUser) {
        ApiResponse<UserAuthorityDetailsResponseDTO> response = authorityService.getMyAuthorities(currentUser);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<UserAuthorityDetailsResponseDTO>> getUserAuthorities(@PathVariable Long userId) {
        ApiResponse<UserAuthorityDetailsResponseDTO> response = authorityService.getUserAuthorities(userId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
