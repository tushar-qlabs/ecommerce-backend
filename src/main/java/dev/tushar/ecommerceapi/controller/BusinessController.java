package dev.tushar.ecommerceapi.controller;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.BusinessRegistrationRequestDTO;
import dev.tushar.ecommerceapi.dto.response.BusinessResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('CREATE_BUSINESS')")
    public ResponseEntity<ApiResponse<BusinessResponseDTO>> registerBusiness(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody BusinessRegistrationRequestDTO request) {
        ApiResponse<BusinessResponseDTO> response = businessService.registerBusiness(currentUser, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // It should be fundamental right, for everyone to read their own details.
    public ResponseEntity<ApiResponse<BusinessResponseDTO>> getMyBusiness(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ApiResponse<BusinessResponseDTO> response = businessService.getMyBusiness(currentUser);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_ALL_BUSINESSES')")
    public ResponseEntity<ApiResponse<List<BusinessResponseDTO>>> getAllBusinesses() {
        ApiResponse<List<BusinessResponseDTO>> response = businessService.getAllBusinesses();
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{businessId}")
    @PreAuthorize("hasAuthority('READ_ALL_BUSINESSES')")
    public ResponseEntity<ApiResponse<BusinessResponseDTO>> getBusinessById(@PathVariable Long businessId) {
        ApiResponse<BusinessResponseDTO> response = businessService.getBusinessById(businessId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
