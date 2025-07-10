package dev.tushar.ecommerceapi.service.impl;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.BusinessRegistrationRequestDTO;
import dev.tushar.ecommerceapi.dto.response.BusinessResponseDTO;
import dev.tushar.ecommerceapi.entity.Business;
import dev.tushar.ecommerceapi.entity.Role;
import dev.tushar.ecommerceapi.entity.User;
import dev.tushar.ecommerceapi.exception.BusinessAlreadyExistException;
import dev.tushar.ecommerceapi.exception.ResourceNotFoundException;
import dev.tushar.ecommerceapi.model.VerificationStatus;
import dev.tushar.ecommerceapi.repository.BusinessRepository;
import dev.tushar.ecommerceapi.repository.RoleRepository;
import dev.tushar.ecommerceapi.repository.UserRepository;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public ApiResponse<BusinessResponseDTO> registerBusiness(CustomUserDetails currentUser, BusinessRegistrationRequestDTO request) {
        User user = currentUser.user();

        if (businessRepository.existsByUserId(user.getId())) {
            throw new BusinessAlreadyExistException("User already has a registered business.");
        }

        Business business = Business.builder()
                .user(user)
                .businessName(request.businessName())
                .businessDescription(request.businessDescription())
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

    @Transactional
    @Override
    public ApiResponse<BusinessResponseDTO> updateBusinessValidationStatus(Long businessId, String status) {
        VerificationStatus statusEnum;
        try {
            statusEnum = VerificationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        business.setVerificationStatus(statusEnum.name());
        Business savedBusiness = businessRepository.save(business);

        if (statusEnum == VerificationStatus.VERIFIED) {
            User user = savedBusiness.getUser();
            if (user == null) {
                throw new ResourceNotFoundException("User associated with this business not found.");
            }

            Role sellerRole = roleRepository.findByName("SELLER")
                    .orElseThrow(() -> new ResourceNotFoundException("SELLER: Role not found."));

            if (user.getRoles().stream().noneMatch(role -> role.getName().equals("SELLER"))) {
                user.getRoles().add(sellerRole);
                userRepository.save(user);
            }
        }

        return ApiResponse.success("Business validated successfully.",
                mapToBusinessResponseDTO(savedBusiness),
                HttpStatus.OK.value());
    }


    // Should have used MapStruct instead of this manual shit.
    private BusinessResponseDTO mapToBusinessResponseDTO(Business business) {
        return new BusinessResponseDTO(
                business.getId(),
                business.getBusinessName(),
                business.getBusinessDescription(),
                business.getVerificationStatus(),
                business.getUser().getId()
        );
    }
}
