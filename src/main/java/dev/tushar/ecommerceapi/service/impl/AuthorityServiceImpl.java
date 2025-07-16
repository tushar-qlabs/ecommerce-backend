package dev.tushar.ecommerceapi.service.impl;

import dev.tushar.ecommerceapi.dto.request.RoleRequestDTO;
import dev.tushar.ecommerceapi.dto.request.UpdateUserPermissionsRequestDTO;
import dev.tushar.ecommerceapi.dto.request.UpdateUserRolesRequestDTO;
import dev.tushar.ecommerceapi.dto.response.PermissionResponseDTO;
import dev.tushar.ecommerceapi.dto.response.RoleResponseDTO;
import dev.tushar.ecommerceapi.dto.response.UserAuthorityDetailsResponseDTO;
import dev.tushar.ecommerceapi.entity.Permission;
import dev.tushar.ecommerceapi.entity.Role;
import dev.tushar.ecommerceapi.entity.User;
import dev.tushar.ecommerceapi.exception.ApiException;
import dev.tushar.ecommerceapi.repository.PermissionRepository;
import dev.tushar.ecommerceapi.repository.RoleRepository;
import dev.tushar.ecommerceapi.repository.UserRepository;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.AuthorityService;
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
    public List<RoleResponseDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToRoleResponseDTO)
                .toList();
    }

    @Override
    public RoleResponseDTO getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "A role with the provided ID could not be found."
        ));
        return mapToRoleResponseDTO(role);
    }

    @Override
    public RoleResponseDTO createRole(RoleRequestDTO roleRequest) {
        Set<Permission> permissions = getPermissionsFromIds(roleRequest.getPermissionIds());
        Role role = Role.builder()
                .name(roleRequest.getName().toUpperCase())
                .permissions(permissions)
                .build();
        Role savedRole = roleRepository.save(role);
        return mapToRoleResponseDTO(savedRole);
    }

    @Override
    public RoleResponseDTO updateRole(Long roleId, RoleRequestDTO roleRequest) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "A role with the provided ID could not be found."
        ));
        Set<Permission> permissions = getPermissionsFromIds(roleRequest.getPermissionIds());
        role.setName(roleRequest.getName().toUpperCase());
        role.setPermissions(permissions);
        Role updatedRole = roleRepository.save(role);
        return mapToRoleResponseDTO(updatedRole);
    }

    @Override
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "A role with the provided ID could not be found."
        ));
        roleRepository.delete(role);
    }

    @Override
    public List<PermissionResponseDTO> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionResponseDTO(p.getId(), p.getName()))
                .toList();
    }

    @Override
    public PermissionResponseDTO getPermissionById(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "A permission with the provided ID could not be found."
        ));
        return new PermissionResponseDTO(permission.getId(), permission.getName());
    }

    @Override
    public void updateUserRoles(Long userId, UpdateUserRolesRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "A user with the provided ID could not be found."
                ));

        Set<Role> newRoles = request.roleIds().stream()
                .map(roleId -> roleRepository.findById(roleId.longValue())
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "A role with the provided ID could not be found."
                        )))
                .collect(Collectors.toSet());

        user.setRoles(newRoles);
        userRepository.save(user);
    }

    @Override
    public void updateUserPermissions(Long userId, UpdateUserPermissionsRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "A user with the provided ID could not be found."
                ));

        Set<Permission> newPermissions = request.permissionIds().stream()
                .map(permissionId -> permissionRepository.findById(permissionId.longValue())
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "A permission with the provided ID could not be found."
                        )))
                .collect(Collectors.toSet());

        user.setPermissions(newPermissions);
        userRepository.save(user);
    }

    @Override
    public UserAuthorityDetailsResponseDTO getMyAuthorities(CustomUserDetails currentUser) {
        User user = currentUser.user();
        return buildUserAuthorityDetails(user);
    }

    @Override
    public UserAuthorityDetailsResponseDTO getUserAuthorities(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "A user with the provided ID could not be found."
                ));
        return buildUserAuthorityDetails(user);
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
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "A permission with the provided ID could not be found."
                        )))
                .collect(Collectors.toSet());
    }

    private RoleResponseDTO mapToRoleResponseDTO(Role role) {
        Set<PermissionResponseDTO> permissions = role.getPermissions().stream()
                .map(p -> new PermissionResponseDTO(p.getId(), p.getName()))
                .collect(Collectors.toSet());
        return new RoleResponseDTO(role.getId(), role.getName(), permissions);
    }
}