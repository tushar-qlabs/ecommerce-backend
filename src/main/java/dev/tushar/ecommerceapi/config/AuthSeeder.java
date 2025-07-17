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
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static dev.tushar.ecommerceapi.model.PermissionKey.CREATE_BUSINESS;

@Component
@Order(1) // Runs first
@Transactional
@RequiredArgsConstructor
public class AuthSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BusinessRepository businessRepository;
    private final PermissionRepository permissionRepository;


    @Override
    public void run(String... args) throws Exception {
        // --- Create All Permissions from the Enum ---
        for (PermissionKey p : PermissionKey.values()) {
            createPermissionIfNotFound(p.name());
        }

        // --- Create Role-Permission Mappings ---
        createRoleIfNotFound("CUSTOMER", Set.of(
                PermissionKey.UPDATE_MY_PROFILE, PermissionKey.CREATE_ORDERS
        ));
        createRoleIfNotFound("SELLER", Set.of(
                PermissionKey.UPDATE_MY_PROFILE, PermissionKey.CREATE_ORDERS, PermissionKey.CREATE_PRODUCTS, PermissionKey.UPDATE_PRODUCTS,
                PermissionKey.DELETE_PRODUCTS, PermissionKey.READ_SELLER_ORDERS, PermissionKey.UPDATE_SELLER_ORDERS
        ));
        createRoleIfNotFound("ADMIN", Set.of(PermissionKey.values()));

        // --- Create Default User Accounts ---
        createAdminUserIfNotFound();
        createSellerUserIfNotFound();
        createSecondSellerUserIfNotFound();
        createCustomerUserIfNotFound();
    }

    // THIS IS MY ADMIN USER
    private void createAdminUserIfNotFound() {
        String adminEmail = "admin@ecom.in";
        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            User adminUser = User.builder()
                    .firstName("Admin")
                    .lastName("User")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("Admin/1234"))
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(adminUser);
        }
    }

    // THIS IS MY FIRST SELLER USER (MOCK DATA)
    private void createSellerUserIfNotFound() {
        String sellerEmail = "seller@ecom.in";
        if (!userRepository.existsByEmail(sellerEmail)) {
            Role sellerRole = roleRepository.findByName("SELLER").orElseThrow();
            Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();
            Permission createBusinessPermission = permissionRepository.findByName(CREATE_BUSINESS.name()).orElseThrow();

            User sellerUser = User.builder()
                    .firstName("Fashion")
                    .lastName("Vendor")
                    .email(sellerEmail)
                    .passwordHash(passwordEncoder.encode("Seller/1234"))
                    .roles(Set.of(sellerRole, customerRole)) // This user has both SELLER and CUSTOMER roles (VERIFIED)
                    .permissions(Set.of(createBusinessPermission))
                    .build();
            userRepository.save(sellerUser);

            Business business = Business.builder()
                    .user(sellerUser)
                    .businessName("Fashion Fusion")
                    .businessDescription("The best clothing and apparel.")
                    .verificationStatus("VERIFIED")
                    .build();
            businessRepository.save(business);
        }
    }

    // THIS IS MY SECOND SELLER USER (MOCK DATA)
    private void createSecondSellerUserIfNotFound() {
        String sellerEmail = "seller2@ecom.in";
        if (!userRepository.existsByEmail(sellerEmail)) {
            Role sellerRole = roleRepository.findByName("SELLER").orElseThrow();
            Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();
            Permission createBusinessPermission = permissionRepository.findByName(CREATE_BUSINESS.name()).orElseThrow();

            User sellerUser = User.builder()
                    .firstName("Urban")
                    .lastName("Weave")
                    .email(sellerEmail)
                    .passwordHash(passwordEncoder.encode("Seller2/1234"))
                    .roles(Set.of(sellerRole, customerRole)) // This user has both SELLER and CUSTOMER roles (VERIFIED)
                    .permissions(Set.of(createBusinessPermission))
                    .build();
            userRepository.save(sellerUser);

            Business business = Business.builder()
                    .user(sellerUser)
                    .businessName("Urban Weave")
                    .businessDescription("Latest and greatest collection of clothes.")
                    .verificationStatus("VERIFIED")
                    .build();
            businessRepository.save(business);
        }
    }

    // THIS IS MY CUSTOMER USER
    private void createCustomerUserIfNotFound() {
        String customerEmail = "customer@ecom.in";
        if (!userRepository.existsByEmail(customerEmail)) {
            Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();
            Permission createBusinessPermission = permissionRepository.findByName(CREATE_BUSINESS.name()).orElseThrow();
            User customerUser = User.builder()
                    .firstName("Customer")
                    .lastName("User")
                    .email(customerEmail)
                    .passwordHash(passwordEncoder.encode("Customer/1234"))
                    .roles(Set.of(customerRole))
                    .permissions(Set.of(createBusinessPermission))
                    .build();
            userRepository.save(customerUser);
        }
    }

    // HERE WE CREATING THE PERMISSIONS
    private void createPermissionIfNotFound(String name) {
        permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(Permission.builder().name(name).build()));
    }

    // HERE WE CREATING THE ROLES
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