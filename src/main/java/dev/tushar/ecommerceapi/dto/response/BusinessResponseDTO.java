package dev.tushar.ecommerceapi.dto.response;

public record BusinessResponseDTO(
        Long id,
        String businessName,
        String verificationStatus,
        Long ownerId
) {}

