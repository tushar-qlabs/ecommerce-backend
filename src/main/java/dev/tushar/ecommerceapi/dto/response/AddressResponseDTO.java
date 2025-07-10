package dev.tushar.ecommerceapi.dto.response;

public record AddressResponseDTO(
        Long id,
        String label,
        String streetLine1,
        String streetLine2,
        String city,
        String state,
        String postalCode,
        String countryCode
) {}
