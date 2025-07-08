package dev.tushar.ecommerceapi.config;

import dev.tushar.ecommerceapi.entity.Permission;
import dev.tushar.ecommerceapi.entity.Role;
import dev.tushar.ecommerceapi.entity.User;
import dev.tushar.ecommerceapi.model.PermissionKey;
import dev.tushar.ecommerceapi.repository.PermissionRepository;
import dev.tushar.ecommerceapi.repository.RoleRepository;
import dev.tushar.ecommerceapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static dev.tushar.ecommerceapi.model.PermissionKey.*;

@Component
@RequiredArgsConstructor
public class SeedDataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // --- Create All Permissions from the Enum ---
        for (PermissionKey p : PermissionKey.values()) {
            createPermissionIfNotFound(p.name());
        }

        // --- Create Role-Permission Mappings ---
        createRoleIfNotFound("CUSTOMER", Set.of(
                READ_MY_PROFILE, UPDATE_MY_PROFILE,
                READ_PRODUCTS, CREATE_ORDERS, READ_MY_ORDERS
        ));
        createRoleIfNotFound("SELLER", Set.of(
                READ_MY_PROFILE, UPDATE_MY_PROFILE,
                READ_PRODUCTS, CREATE_ORDERS, READ_MY_ORDERS,
                CREATE_BUSINESS, READ_MY_BUSINESS,
                CREATE_PRODUCTS, UPDATE_PRODUCTS, DELETE_PRODUCTS,
                READ_SELLER_ORDERS, UPDATE_SELLER_ORDERS
        ));
        createRoleIfNotFound("ADMIN", Set.of(PermissionKey.values()));

        // --- Create Default User Accounts ---
        createAdminUserIfNotFound();
        createSellerUserIfNotFound();
        createCustomerUserIfNotFound();
        createHybridUserIfNotFound();
    }

    // --- User Creation Methods ---

    private void createAdminUserIfNotFound() {
        String adminEmail = "admin@ecom.in";
        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            User adminUser = User.builder()
                    .firstName("Admin")
                    .lastName("User")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("1234"))
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(adminUser);
        }
    }

    private void createSellerUserIfNotFound() {
        String sellerEmail = "seller@ecom.in";
        if (!userRepository.existsByEmail(sellerEmail)) {
            Role sellerRole = roleRepository.findByName("SELLER").orElseThrow();
            User sellerUser = User.builder()
                    .firstName("Seller")
                    .lastName("User")
                    .email(sellerEmail)
                    .passwordHash(passwordEncoder.encode("1234"))
                    .roles(Set.of(sellerRole))
                    .build();
            userRepository.save(sellerUser);
        }
    }

    private void createCustomerUserIfNotFound() {
        String customerEmail = "customer@ecom.in";
        if (!userRepository.existsByEmail(customerEmail)) {
            Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();
            User customerUser = User.builder()
                    .firstName("Customer")
                    .lastName("User")
                    .email(customerEmail)
                    .passwordHash(passwordEncoder.encode("1234"))
                    .roles(Set.of(customerRole))
                    .build();
            userRepository.save(customerUser);
        }
    }

    /**
     * Creates a user with both CUSTOMER and SELLER roles, along with extra individual
     * permissions. This is useful when a user needs one or more specific permissions,
     * but creating an entire role for them would be redundant. In this case, we can
     * assign permissions directly to the user.
     */
    private void createHybridUserIfNotFound() {
        String hybridEmail = "hybrid@ecom.in";
        if (!userRepository.existsByEmail(hybridEmail)) {
            Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();
            Role sellerRole = roleRepository.findByName("SELLER").orElseThrow();

            Permission readUsersPermission = permissionRepository.findByName(READ_ALL_USERS.name()).orElseThrow();
            Permission manageRolesPermission = permissionRepository.findByName(MANAGE_ROLES.name()).orElseThrow();

            User hybridUser = User.builder()
                    .firstName("Hybrid")
                    .lastName("User")
                    .email(hybridEmail)
                    .passwordHash(passwordEncoder.encode("1234"))
                    .roles(Set.of(customerRole, sellerRole))
                    .permissions(Set.of(readUsersPermission, manageRolesPermission))
                    .build();
            userRepository.save(hybridUser);
        }
    }

    // --- Helper Methods ---

    private void createPermissionIfNotFound(String name) {
        permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(Permission.builder().name(name).build()));
    }

    private void createRoleIfNotFound(String name, Set<PermissionKey> permissions) {
        roleRepository.findByName(name).orElseGet(() -> {
            Set<Permission> perms = permissions.stream()
                    .map(p -> permissionRepository.findByName(p.name()).orElseThrow())
                    .collect(Collectors.toSet());
            Role role = Role.builder().name(name).permissions(perms).build();
            return roleRepository.save(role);
        });
    }
}