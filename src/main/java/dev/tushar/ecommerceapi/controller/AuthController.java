package dev.tushar.ecommerceapi.controller;

import dev.tushar.ecommerceapi.dto.request.AuthenticationRequest;
import dev.tushar.ecommerceapi.dto.request.RegisterRequest;
import dev.tushar.ecommerceapi.dto.response.AuthResponse;
import dev.tushar.ecommerceapi.dto.response.RegisterResponse;
import dev.tushar.ecommerceapi.service.AuthService;
import dev.tushar.ecommerceapi.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @RequestBody @Valid RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticate(
            @RequestBody @Valid AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}