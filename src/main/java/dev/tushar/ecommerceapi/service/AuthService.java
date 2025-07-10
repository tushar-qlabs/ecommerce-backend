package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.AuthRequestDTO;
import dev.tushar.ecommerceapi.dto.request.RegisterRequestDTO;
import dev.tushar.ecommerceapi.dto.response.LoginResponseDTO;
import dev.tushar.ecommerceapi.dto.response.RegisterResponseDTO;
import dev.tushar.ecommerceapi.entity.Role;
import dev.tushar.ecommerceapi.entity.Permission;
import dev.tushar.ecommerceapi.entity.User;
import dev.tushar.ecommerceapi.exception.ApiException;
import dev.tushar.ecommerceapi.repository.PermissionRepository;
import dev.tushar.ecommerceapi.repository.RoleRepository;
import dev.tushar.ecommerceapi.repository.UserRepository;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public ApiResponse<RegisterResponseDTO> register(RegisterRequestDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "An account with the email " + request.getEmail() + " already exists."
            );
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Server configuration error: The 'CUSTOMER' role is missing."
                ));

        Permission createBusinessPermission = permissionRepository.findByName("CREATE_BUSINESS")
                .orElseThrow(() -> new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Server configuration error: The 'CREATE_BUSINESS' permission is missing."
                ));
        var user = User.builder()
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(customerRole))
                .permissions(Set.of(createBusinessPermission))
                .build();

        user = userRepository.save(user);
        return ApiResponse.success(
                "Registration successful.",
                new RegisterResponseDTO(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail()
                ),
                HttpStatus.OK.value()
        );
    }

    @Transactional
    public ApiResponse<LoginResponseDTO> authenticate(AuthRequestDTO request) {

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
            )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "The user with ID " + request.getEmail() + " could not be found."
        ));

        String jwtToken = jwtUtil.generateToken(new CustomUserDetails(user));

        return ApiResponse.success(
                "Login successful.",
                new LoginResponseDTO(
                        user.getId(),
                        user.getFirstName(),
                        user.getEmail(),
                        new HashMap<>(
                                Map.of(
                                        "token", jwtToken
                                )
                        )
                ),
                HttpStatus.OK.value()
        );
    }
}
