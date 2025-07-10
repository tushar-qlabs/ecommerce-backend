package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.RoleRequestDTO;
import dev.tushar.ecommerceapi.dto.request.UpdateUserPermissionsRequestDTO;
import dev.tushar.ecommerceapi.dto.request.UpdateUserRolesRequestDTO;
import dev.tushar.ecommerceapi.dto.response.PermissionResponseDTO;
import dev.tushar.ecommerceapi.dto.response.RoleResponseDTO;
import dev.tushar.ecommerceapi.dto.response.UserAuthorityDetailsResponseDTO;
import dev.tushar.ecommerceapi.entity.Permission;
import dev.tushar.ecommerceapi.entity.Role;
import dev.tushar.ecommerceapi.entity.User;
import dev.tushar.ecommerceapi.exception.ResourceNotFoundException;
import dev.tushar.ecommerceapi.repository.PermissionRepository;
import dev.tushar.ecommerceapi.repository.RoleRepository;
import dev.tushar.ecommerceapi.repository.UserRepository;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorityServiceImpl implements AuthorityService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public ApiResponse<List<RoleResponseDTO>> getAllRoles() {
        List<RoleResponseDTO> roles = roleRepository.findAll().stream()
                .map(this::mapToRoleResponseDTO)
                .toList();
        return ApiResponse.success("Roles fetched successfully", roles, HttpStatus.OK.value());
    }

    @Override
    public ApiResponse<RoleResponseDTO> getRoleById(Integer roleId) {
        Role role = roleRepository.findById(roleId.longValue()).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        return ApiResponse.success("Role fetched successfully", mapToRoleResponseDTO(role), HttpStatus.OK.value());
    }

    @Override
    public ApiResponse<RoleResponseDTO> createRole(RoleRequestDTO roleRequest) {
        Set<Permission> permissions = getPermissionsFromIds(roleRequest.getPermissionIds());
        Role role = Role.builder()
                .name(roleRequest.getName().toUpperCase())
                .permissions(permissions)
                .build();
        Role savedRole = roleRepository.save(role);
        return ApiResponse.success("Role created successfully", mapToRoleResponseDTO(savedRole), HttpStatus.CREATED.value());
    }

    @Override
    public ApiResponse<RoleResponseDTO> updateRole(Integer roleId, RoleRequestDTO roleRequest) {
        Role role = roleRepository.findById(roleId.longValue()).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        Set<Permission> permissions = getPermissionsFromIds(roleRequest.getPermissionIds());
        role.setName(roleRequest.getName().toUpperCase());
        role.setPermissions(permissions);
        Role updatedRole = roleRepository.save(role);
        return ApiResponse.success("Role updated successfully", mapToRoleResponseDTO(updatedRole), HttpStatus.OK.value());
    }

    @Override
    public ApiResponse<Void> deleteRole(Integer roleId) {
        Role role = roleRepository.findById(roleId.longValue()).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        roleRepository.delete(role);
        return ApiResponse.success("Role deleted successfully", null, HttpStatus.OK.value());
    }

    @Override
    public ApiResponse<List<PermissionResponseDTO>> getAllPermissions() {
        List<PermissionResponseDTO> permissions = permissionRepository.findAll().stream()
                .map(p -> new PermissionResponseDTO(p.getId(), p.getName()))
                .toList();
        return ApiResponse.success("Permissions fetched successfully", permissions, HttpStatus.OK.value());
    }

    @Override
    public ApiResponse<PermissionResponseDTO> getPermissionById(Integer permissionId) {
        Permission permission = permissionRepository.findById(permissionId.longValue()).orElseThrow(() -> new ResourceNotFoundException("Permission not found"));
        return ApiResponse.success("Permission fetched successfully", new PermissionResponseDTO(permission.getId(), permission.getName()), HttpStatus.OK.value());
    }

    @Override
    public ApiResponse<Void> updateUserRoles(Long userId, UpdateUserRolesRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Set<Role> newRoles = request.roleIds().stream()
                .map(roleId -> roleRepository.findById(roleId.longValue())
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId)))
                .collect(Collectors.toSet());

        user.setRoles(newRoles);
        userRepository.save(user);

        return ApiResponse.success("User roles updated successfully", null, HttpStatus.OK.value());
    }

    @Override
    public ApiResponse<Void> updateUserPermissions(Long userId, UpdateUserPermissionsRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Set<Permission> newPermissions = request.permissionIds().stream()
                .map(permissionId -> permissionRepository.findById(permissionId.longValue())
                        .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permissionId)))
                .collect(Collectors.toSet());

        user.setPermissions(newPermissions);
        userRepository.save(user);

        return ApiResponse.success("User permissions updated successfully", null, HttpStatus.OK.value());
    }

    @Override
    public ApiResponse<UserAuthorityDetailsResponseDTO> getMyAuthorities(CustomUserDetails currentUser) {
        User user = currentUser.user();
        UserAuthorityDetailsResponseDTO authorities = buildUserAuthorityDetails(user);
        return ApiResponse.success("Authorities fetched successfully", authorities, HttpStatus.OK.value());
    }

    @Override
    public ApiResponse<UserAuthorityDetailsResponseDTO> getUserAuthorities(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        UserAuthorityDetailsResponseDTO authorities = buildUserAuthorityDetails(user);
        return ApiResponse.success("Authorities for user " + userId + " fetched successfully", authorities, HttpStatus.OK.value());
    }

    private UserAuthorityDetailsResponseDTO buildUserAuthorityDetails(User user) {
        Set<UserAuthorityDetailsResponseDTO.RoleDetail> assignedRoles = user.getRoles().stream()
                .map(role -> new UserAuthorityDetailsResponseDTO.RoleDetail(role.getId(), role.getName()))
                .collect(Collectors.toSet());

        Set<UserAuthorityDetailsResponseDTO.PermissionDetail> individualPermissions = user.getPermissions().stream()
                .map(permission -> new UserAuthorityDetailsResponseDTO.PermissionDetail(permission.getId(), permission.getName()))
                .collect(Collectors.toSet());

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Set<String> allEffectivePermissions = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .filter(s -> !s.startsWith("ROLE_"))
                .collect(Collectors.toSet());

        return new UserAuthorityDetailsResponseDTO(assignedRoles, individualPermissions, allEffectivePermissions);
    }

    private Set<Permission> getPermissionsFromIds(Set<Integer> permissionIds) {
        return permissionIds.stream()
                .map(id -> permissionRepository.findById(id.longValue())
                        .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + id)))
                .collect(Collectors.toSet());
    }

    private RoleResponseDTO mapToRoleResponseDTO(Role role) {
        Set<PermissionResponseDTO> permissions = role.getPermissions().stream()
                .map(p -> new PermissionResponseDTO(p.getId(), p.getName()))
                .collect(Collectors.toSet());
        return new RoleResponseDTO(role.getId(), role.getName(), permissions);
    }
}
