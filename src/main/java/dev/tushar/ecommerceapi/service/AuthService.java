package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.AuthenticationRequest;
import dev.tushar.ecommerceapi.dto.request.RegisterRequest;
import dev.tushar.ecommerceapi.dto.response.AuthResponse;
import dev.tushar.ecommerceapi.dto.response.RegisterResponse;
import dev.tushar.ecommerceapi.entity.Role;
import dev.tushar.ecommerceapi.entity.User;
import dev.tushar.ecommerceapi.exception.EmailOrPhoneAlreadyExistsException;
import dev.tushar.ecommerceapi.exception.UserNotFoundException;
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
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public ApiResponse<RegisterResponse> register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw EmailOrPhoneAlreadyExistsException.forEmail("Registration Failed", request.getEmail());
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Error: CUSTOMER role not found."));

        var user = User.builder()
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(customerRole))
                .build();

        user = userRepository.save(user);
        return ApiResponse.success(
                "Registration successful.",
                new RegisterResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail()
                ),
                HttpStatus.CREATED.value()
        );
    }

    @Transactional
    public ApiResponse<AuthResponse> authenticate(AuthenticationRequest request) {

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
            )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        String jwtToken = jwtUtil.generateToken(new CustomUserDetails(user));

        return ApiResponse.success(
                "Login successful.",
                new AuthResponse(
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
