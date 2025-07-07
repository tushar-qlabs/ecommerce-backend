package dev.tushar.ecommerceapi.config;

import dev.tushar.ecommerceapi.entity.Permission;
import dev.tushar.ecommerceapi.entity.Role;
import dev.tushar.ecommerceapi.entity.User;
import dev.tushar.ecommerceapi.repository.PermissionRepository;
import dev.tushar.ecommerceapi.repository.RoleRepository;
import dev.tushar.ecommerceapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
        // --- Create Permissions ---
        Permission readProducts = createPermissionIfNotFound("READ_PRODUCTS");
        Permission writeProducts = createPermissionIfNotFound("WRITE_PRODUCTS");
        Permission readOrders = createPermissionIfNotFound("READ_ORDERS");
        Permission writeOrders = createPermissionIfNotFound("WRITE_ORDERS");
        Permission readUsers = createPermissionIfNotFound("READ_USERS");

        // --- Create Roles ---
        Role adminRole = createRoleIfNotFound("ADMIN", new HashSet<>(Arrays.asList(
                readProducts, writeProducts, readOrders, writeOrders, readUsers
        )));
        Role sellerRole = createRoleIfNotFound("SELLER", new HashSet<>(Arrays.asList(
                readProducts, writeProducts, readOrders
        )));
        Role customerRole = createRoleIfNotFound("CUSTOMER", new HashSet<>(Collections.singletonList(
                readProducts
        )));

        // --- Create a seller account ---
        createSellerAccountIfNotFound(
                new HashSet<>(Arrays.asList(customerRole, sellerRole)),
                new HashSet<>(Arrays.asList(readUsers, writeOrders))
        );
    }

    private Permission createPermissionIfNotFound(String name) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(Permission.builder().name(name).build()));
    }

    private void createSellerAccountIfNotFound(Set<Role> roles, Set<Permission> permissions) {
        String sellerEmail = "seller@ecom.in";
        if (!userRepository.existsByEmail(sellerEmail)) {
            User sellerUser = User.builder()
                    .firstName("Seller")
                    .lastName("Account")
                    .email(sellerEmail)
                    .passwordHash(passwordEncoder.encode("1234"))
                    .roles(roles)
                    .permissions(permissions)
                    .build();
            userRepository.save(sellerUser);
        }
    }

    private Role createRoleIfNotFound(String name, Set<Permission> permissions) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = Role.builder().name(name).permissions(permissions).build();
            return roleRepository.save(role);
        });
    }
}
