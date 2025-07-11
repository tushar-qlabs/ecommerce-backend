package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.BusinessRegistrationRequestDTO;
import dev.tushar.ecommerceapi.dto.response.BusinessResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import java.util.List;

public interface BusinessService {

    // Current user operations
    ApiResponse<BusinessResponseDTO> registerBusiness(CustomUserDetails currentUser, BusinessRegistrationRequestDTO request);
    ApiResponse<BusinessResponseDTO> getMyBusiness(CustomUserDetails currentUser);

    // Admin user operations
    ApiResponse<List<BusinessResponseDTO>> getAllBusinesses();
    ApiResponse<BusinessResponseDTO> getBusinessById(Long businessId);
    ApiResponse<BusinessResponseDTO> updateBusinessValidationStatus(Long businessId, String status);
}
