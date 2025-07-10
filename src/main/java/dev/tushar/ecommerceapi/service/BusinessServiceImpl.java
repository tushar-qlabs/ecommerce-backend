package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.BusinessRegistrationRequestDTO;
import dev.tushar.ecommerceapi.dto.response.BusinessResponseDTO;
import dev.tushar.ecommerceapi.entity.Business;
import dev.tushar.ecommerceapi.entity.User;
import dev.tushar.ecommerceapi.exception.ConflictException;
import dev.tushar.ecommerceapi.exception.ResourceNotFoundException;
import dev.tushar.ecommerceapi.repository.BusinessRepository;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;

    @Override
    public ApiResponse<BusinessResponseDTO> registerBusiness(CustomUserDetails currentUser, BusinessRegistrationRequestDTO request) {
        User user = currentUser.user();

        if (businessRepository.existsByUserId(user.getId())) {
            throw new ConflictException("User already has a registered business.");
        }

        Business business = Business.builder()
                .user(user)
                .businessName(request.businessName())
                .verificationStatus("PENDING")
                .build();

        Business savedBusiness = businessRepository.save(business);
        return ApiResponse.success("Business registered successfully. Awaiting verification.", mapToBusinessResponseDTO(savedBusiness), HttpStatus.CREATED.value());
    }

    @Override
    public ApiResponse<BusinessResponseDTO> getMyBusiness(CustomUserDetails currentUser) {
        User user = currentUser.user();
        Business business = businessRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No business found for the current user."));
        return ApiResponse.success("Business details fetched successfully.", mapToBusinessResponseDTO(business), HttpStatus.OK.value());
    }

    @Override
    public ApiResponse<List<BusinessResponseDTO>> getAllBusinesses() {
        List<BusinessResponseDTO> businesses = businessRepository.findAll().stream()
                .map(this::mapToBusinessResponseDTO)
                .collect(Collectors.toList());
        return ApiResponse.success("All businesses fetched successfully.", businesses, HttpStatus.OK.value());
    }

    @Override
    public ApiResponse<BusinessResponseDTO> getBusinessById(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));
        return ApiResponse.success("Business details fetched successfully.", mapToBusinessResponseDTO(business), HttpStatus.OK.value());
    }

    // Should use MapStruct instead, but damn no fkn time!
    private BusinessResponseDTO mapToBusinessResponseDTO(Business business) {
        return new BusinessResponseDTO(
                business.getId(),
                business.getBusinessName(),
                business.getVerificationStatus(),
                business.getUser().getId()
        );
    }
}
