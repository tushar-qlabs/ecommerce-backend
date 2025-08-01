package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.request.BusinessRegistrationRequestDTO;
import dev.tushar.ecommerceapi.dto.request.BusinessUpdateRequestDTO;
import dev.tushar.ecommerceapi.dto.response.BusinessResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import java.util.List;

public interface BusinessService {

    // Current user operations
    BusinessResponseDTO registerBusiness(CustomUserDetails currentUser, BusinessRegistrationRequestDTO request);
    BusinessResponseDTO getMyBusiness(CustomUserDetails currentUser);
    BusinessResponseDTO updateMyBusiness(CustomUserDetails currentUser, BusinessUpdateRequestDTO request); // New Method

    // Admin user operations
    List<BusinessResponseDTO> getAllBusinesses();
    BusinessResponseDTO getBusinessById(Long businessId);
    BusinessResponseDTO updateBusinessValidationStatus(Long businessId, String status);
}
