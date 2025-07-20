package dev.tushar.ecommerceapi.service.impl;

import dev.tushar.ecommerceapi.dto.request.BusinessRegistrationRequestDTO;
import dev.tushar.ecommerceapi.dto.request.BusinessUpdateRequestDTO;
import dev.tushar.ecommerceapi.dto.response.BusinessResponseDTO;
import dev.tushar.ecommerceapi.entity.Business;
import dev.tushar.ecommerceapi.entity.Role;
import dev.tushar.ecommerceapi.entity.User;
import dev.tushar.ecommerceapi.exception.ApiException;
import dev.tushar.ecommerceapi.model.VerificationStatus;
import dev.tushar.ecommerceapi.repository.BusinessRepository;
import dev.tushar.ecommerceapi.repository.PermissionRepository;
import dev.tushar.ecommerceapi.repository.RoleRepository;
import dev.tushar.ecommerceapi.repository.UserRepository;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public BusinessResponseDTO registerBusiness(CustomUserDetails currentUser, BusinessRegistrationRequestDTO request) {
        User user = currentUser.user();

        Optional<Business> existingBusinessOpt = businessRepository.findByUserId(user.getId());

        if (existingBusinessOpt.isPresent()) {
            Business existingBusiness = existingBusinessOpt.get();
            String status = existingBusiness.getVerificationStatus();
            String message = String.format(
                    "You have already submitted a business registration. Its current status is: %s.",
                    status
            );
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    message,
                    Map.of("currentStatus", status)
            );
        }

        Business business = Business.builder()
                .user(user)
                .businessName(request.businessName())
                .businessDescription(request.businessDescription())
                .verificationStatus("PENDING")
                .build();

        Business savedBusiness = businessRepository.save(business);
        return mapToBusinessResponseDTO(savedBusiness);
    }

    @Override
    public BusinessResponseDTO getMyBusiness(CustomUserDetails currentUser) {
        User user = currentUser.user();
        Business business = businessRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "No business has been registered for this account."
                ));
        return mapToBusinessResponseDTO(business);
    }

    @Override
    public BusinessResponseDTO updateMyBusiness(CustomUserDetails currentUser, BusinessUpdateRequestDTO request) {
        User user = currentUser.user();
        Business business = businessRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "No business has been registered for this account."
                ));

        if (request.businessName() != null && !request.businessName().isBlank()) {
            business.setBusinessName(request.businessName());
        }
        if (request.businessDescription() != null && !request.businessDescription().isBlank()) {
            business.setBusinessDescription(request.businessDescription());
        }

        Business updatedBusiness = businessRepository.save(business);
        return mapToBusinessResponseDTO(updatedBusiness);
    }

    @Override
    public List<BusinessResponseDTO> getAllBusinesses() {
        return businessRepository.findAll().stream()
                .map(this::mapToBusinessResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BusinessResponseDTO getBusinessById(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "A business with ID " + businessId + " could not be found."
                ));
        return mapToBusinessResponseDTO(business);
    }

    @Override
    public BusinessResponseDTO updateBusinessValidationStatus(Long businessId, String status) {
        VerificationStatus statusEnum;
        try {
            statusEnum = VerificationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "The provided status value is invalid.",
                    Map.of("invalidStatus", status, "allowedValues ", Arrays.toString(VerificationStatus.values()))
            );
        }

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "A business with ID " + businessId + " could not be found."
                ));

        business.setVerificationStatus(statusEnum.name());

        // Side effects that I want to apply when
        // specific verification status is set
        User user = business.getUser();
        if (statusEnum == VerificationStatus.VERIFIED) {
            Role sellerRole = roleRepository.findByName("SELLER")
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Server configuration error: The 'SELLER' role is missing."
                    ));

            if (user.getRoles().stream().noneMatch(role -> role.getName().equals("SELLER"))) {
                user.getRoles().add(sellerRole);
            }
        } else if (statusEnum == VerificationStatus.SUSPENDED) {
            user.getRoles().removeIf(role -> role.getName().equals("SELLER"));
            permissionRepository.findByName("CREATE_BUSINESS").ifPresent(permission -> {
                user.getPermissions().remove(permission);
            });
        }

        userRepository.save(user);
        Business savedBusiness = businessRepository.save(business);

        return mapToBusinessResponseDTO(savedBusiness);
    }

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
