package dev.tushar.ecommerceapi.controller;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.BusinessRegistrationRequestDTO;
import dev.tushar.ecommerceapi.dto.request.ValidateBusinessRequestDTO;
import dev.tushar.ecommerceapi.dto.response.BusinessResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

        BusinessResponseDTO dto = businessService.registerBusiness(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Business registered successfully. Awaiting verification.", dto, HttpStatus.CREATED.value())
        );
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BusinessResponseDTO>> getMyBusiness(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        BusinessResponseDTO dto = businessService.getMyBusiness(currentUser);
        return ResponseEntity.ok(
                ApiResponse.success("Business details fetched successfully.", dto, HttpStatus.OK.value())
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_ALL_BUSINESSES')")
    public ResponseEntity<ApiResponse<List<BusinessResponseDTO>>> getAllBusinesses() {
        List<BusinessResponseDTO> dtoList = businessService.getAllBusinesses();
        return ResponseEntity.ok(
                ApiResponse.success("All businesses fetched successfully.", dtoList, HttpStatus.OK.value())
        );
    }

    @GetMapping("/{businessId}")
    @PreAuthorize("hasAuthority('READ_ALL_BUSINESSES')")
    public ResponseEntity<ApiResponse<BusinessResponseDTO>> getBusinessById(
            @PathVariable Long businessId) {

        BusinessResponseDTO dto = businessService.getBusinessById(businessId);
        return ResponseEntity.ok(
                ApiResponse.success("Business details fetched successfully.", dto, HttpStatus.OK.value())
        );
    }

    @PutMapping("/status/{businessId}")
    @PreAuthorize("hasAuthority('UPDATE_BUSINESS_STATUS')")
    public ResponseEntity<ApiResponse<BusinessResponseDTO>> updateBusinessValidationStatus(
            @PathVariable Long businessId,
            @Valid @RequestBody ValidateBusinessRequestDTO request) {

        BusinessResponseDTO dto = businessService.updateBusinessValidationStatus(businessId, request.validationStatus());
        return ResponseEntity.ok(
                ApiResponse.success("Business validated successfully.", dto, HttpStatus.OK.value())
        );
    }
}
