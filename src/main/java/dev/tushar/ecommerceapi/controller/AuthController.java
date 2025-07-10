package dev.tushar.ecommerceapi.controller;

import dev.tushar.ecommerceapi.dto.request.AuthRequestDTO;
import dev.tushar.ecommerceapi.dto.request.RegisterRequestDTO;
import dev.tushar.ecommerceapi.dto.response.LoginResponseDTO;
import dev.tushar.ecommerceapi.dto.response.RegisterResponseDTO;
import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDTO>> register(
            @RequestBody @Valid RegisterRequestDTO request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> authenticate(
            @RequestBody @Valid AuthRequestDTO request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}