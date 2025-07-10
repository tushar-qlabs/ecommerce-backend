package dev.tushar.ecommerceapi.config;

import dev.tushar.ecommerceapi.entity.Business;
import dev.tushar.ecommerceapi.entity.Permission;
import dev.tushar.ecommerceapi.entity.Role;
import dev.tushar.ecommerceapi.entity.User;
import dev.tushar.ecommerceapi.model.PermissionKey;
import dev.tushar.ecommerceapi.repository.BusinessRepository;
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
    private final BusinessRepository businessRepository;
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
                UPDATE_MY_PROFILE, CREATE_ORDERS
        ));
        createRoleIfNotFound("SELLER", Set.of(
                UPDATE_MY_PROFILE, CREATE_ORDERS, UPDATE_PRODUCTS,
                DELETE_PRODUCTS, READ_SELLER_ORDERS, UPDATE_SELLER_ORDERS
        ));
        createRoleIfNotFound("ADMIN", Set.of(PermissionKey.values()));

        // --- Create Default User Accounts ---
        createAdminUserIfNotFound();
        createSellerUserIfNotFound();
        createCustomerUserIfNotFound();
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
            Business business = Business.builder()
                            .user(sellerUser)
                            .businessName("My Business")
                            .businessDescription("My Business Description")
                            .verificationStatus("VERIFIED") // If means business is already verified
                            .build();
            businessRepository.save(business);
        }
    }

    private void createCustomerUserIfNotFound() {
        String customerEmail = "customer@ecom.in";
        if (!userRepository.existsByEmail(customerEmail)) {
            Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();
            Permission createBusinessPermission = permissionRepository.findByName(CREATE_BUSINESS.name()).orElseThrow();
            User customerUser = User.builder()
                    .firstName("Customer")
                    .lastName("User")
                    .email(customerEmail)
                    .passwordHash(passwordEncoder.encode("1234"))
                    .roles(Set.of(customerRole))
                    .permissions(Set.of(createBusinessPermission))
                    .build();
            userRepository.save(customerUser);
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