package dev.tushar.ecommerceapi.dto.response;

public record BusinessResponseDTO(
        Long id,
        String businessName,
        String businessDescription,
        String verificationStatus,
        Long ownerId
) {}

