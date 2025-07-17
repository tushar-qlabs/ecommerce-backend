package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.request.AuthRequestDTO;
import dev.tushar.ecommerceapi.dto.request.RefreshTokenRequestDTO;
import dev.tushar.ecommerceapi.dto.request.RegisterRequestDTO;
import dev.tushar.ecommerceapi.dto.response.LoginResponseDTO;
import dev.tushar.ecommerceapi.dto.response.RefreshTokenResponseDTO;
import dev.tushar.ecommerceapi.dto.response.RegisterResponseDTO;
import dev.tushar.ecommerceapi.entity.Permission;
import dev.tushar.ecommerceapi.entity.RefreshToken;
import dev.tushar.ecommerceapi.entity.Role;
import dev.tushar.ecommerceapi.entity.User;
import dev.tushar.ecommerceapi.exception.ApiException;
import dev.tushar.ecommerceapi.repository.PermissionRepository;
import dev.tushar.ecommerceapi.repository.RefreshTokenRepository;
import dev.tushar.ecommerceapi.repository.RoleRepository;
import dev.tushar.ecommerceapi.repository.UserRepository;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${max-sessions}")
    private int maxSessions;

    @Value(("${refresh-token-expiration-time}"))
    private long refreshTokenExpirationTime;

    public RegisterResponseDTO register(RegisterRequestDTO request) {
        boolean emailExists = userRepository.existsByEmail(request.getEmail());
        if (emailExists) {
            throw new ApiException(
                    HttpStatus.ACCEPTED,
                    "An account with the provided email already exists."
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
        return new RegisterResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );
    }

    public LoginResponseDTO authenticate(AuthRequestDTO request, String ipAddress, String deviceInfo) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));

        if (request.getSessionIdToInvalidate() != null) {
            RefreshToken tokenToInvalidate = refreshTokenRepository.findById(request.getSessionIdToInvalidate())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session to invalidate not found."));

            if (!tokenToInvalidate.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("You do not have permission to invalidate this session.");
            }

            refreshTokenRepository.delete(tokenToInvalidate);
            return performLogin(user, ipAddress, deviceInfo);
        }

        List<RefreshToken> activeSessions = refreshTokenRepository.findByUser(user);
        if (activeSessions.size() >= maxSessions) {
            List<Map<String, Object>> sessionDetails = activeSessions.stream()
                    .map(rt -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", rt.getId());
                        map.put("deviceInfo", rt.getDeviceInfo());
                        map.put("ipAddress", rt.getIpAddress());
                        map.put("createdAt", rt.getCreatedAt());
                        return map;
                    })
                    .collect(Collectors.toList());
            return new LoginResponseDTO(sessionDetails);
        }
        return performLogin(user, ipAddress, deviceInfo);
    }

    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        RefreshToken oldRefreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token."));

        if (oldRefreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(oldRefreshToken);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token has expired.");
        }

        User user = oldRefreshToken.getUser();
        refreshTokenRepository.delete(oldRefreshToken);

        String newAccessToken = jwtUtil.generateToken(new CustomUserDetails(user));
        String newRefreshTokenString = UUID.randomUUID().toString();
        Instant refreshTokenExpiry = Instant.now().plus(refreshTokenExpirationTime, ChronoUnit.MILLIS);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(newRefreshTokenString)
                .expiryDate(refreshTokenExpiry)
                .ipAddress(oldRefreshToken.getIpAddress())
                .deviceInfo(oldRefreshToken.getDeviceInfo())
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return new RefreshTokenResponseDTO(
                new HashMap<>(Map.of("token", newAccessToken, "expiresIn", jwtUtil.getAccessTokenExpirationTime())),
                newRefreshTokenString,
                refreshTokenExpirationTime
        );
    }

    public void logout(RefreshTokenRequestDTO request) {
        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(refreshTokenRepository::delete);
    }

    private LoginResponseDTO performLogin(User user, String ipAddress, String deviceInfo) {
        String jwtToken = jwtUtil.generateToken(new CustomUserDetails(user));
        String refreshTokenString = UUID.randomUUID().toString();
        Instant refreshTokenExpiry = Instant.now().plus(refreshTokenExpirationTime, ChronoUnit.MILLIS);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenString)
                .expiryDate(refreshTokenExpiry)
                .ipAddress(ipAddress)
                .deviceInfo(deviceInfo)
                .build();
        refreshTokenRepository.save(refreshToken);

        return new LoginResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getEmail(),
                new HashMap<>(Map.of("token", jwtToken, "expiresIn", jwtUtil.getAccessTokenExpirationTime())),
                refreshTokenString,
                refreshTokenExpirationTime
        );
    }
}