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
        return ResponseEntity.ok(authorityService.getAllRoles());
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> addRole(
            @Valid @RequestBody RoleRequestDTO request
    ) {
        return ResponseEntity.ok(authorityService.createRole(request));
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> getRoleById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(authorityService.getRoleById(id));
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequestDTO request
    ) {
        return ResponseEntity.ok(authorityService.updateRole(id, request));
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(authorityService.deleteRole(id));
    }

    // --- Permission Endpoints ---
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<List<PermissionResponseDTO>>> getPermissions() {;
        return ResponseEntity.ok(authorityService.getAllPermissions());
    }

    @GetMapping("/permissions/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<PermissionResponseDTO>> getPermissionById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(authorityService.getPermissionById(id));
    }

    // --- Assignment Endpoints ---
    @PutMapping("/roles/assign/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<Void>> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequestDTO request) {
        return ResponseEntity.ok(authorityService.updateUserRoles(userId, request));
    }

    @PutMapping("/permissions/assign/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<Void>> updateUserPermissions(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserPermissionsRequestDTO request) {
        return ResponseEntity.ok(authorityService.updateUserPermissions(userId, request));
    }

    // --- User Authority Endpoints ---
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserAuthorityDetailsResponseDTO>> getMyAuthorities(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(authorityService.getMyAuthorities(currentUser));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<UserAuthorityDetailsResponseDTO>> getUserAuthorities(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(authorityService.getUserAuthorities(userId));
    }
}
