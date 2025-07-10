package dev.tushar.ecommerceapi.controller;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.BusinessRegistrationRequestDTO;
import dev.tushar.ecommerceapi.dto.request.ValidateBusinessRequestDTO;
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
        return ResponseEntity.ok(
                businessService.registerBusiness(currentUser, request)
        );
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // Authenticated users can access their own business profile
    public ResponseEntity<ApiResponse<BusinessResponseDTO>> getMyBusiness(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(
                businessService.getMyBusiness(currentUser)
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_ALL_BUSINESSES')")
    public ResponseEntity<ApiResponse<List<BusinessResponseDTO>>> getAllBusinesses() {
        return ResponseEntity.ok(
                businessService.getAllBusinesses()
        );
    }

    @GetMapping("/{businessId}")
    @PreAuthorize("hasAuthority('READ_ALL_BUSINESSES')")
    public ResponseEntity<ApiResponse<BusinessResponseDTO>> getBusinessById(
            @PathVariable Long businessId)
    {
        return ResponseEntity.ok(
                businessService.getBusinessById(businessId)
        );
    }

    @PutMapping("/status/{businessId}")
    @PreAuthorize("hasAuthority('UPDATE_BUSINESS_STATUS')")
    public ResponseEntity<ApiResponse<BusinessResponseDTO>> updateBusinessValidationStatus(
            @PathVariable Long businessId,
            @Valid @RequestBody ValidateBusinessRequestDTO request
    ) {
        return ResponseEntity.ok(
                businessService.updateBusinessValidationStatus(businessId, request.validationStatus())
        );
    }
}
