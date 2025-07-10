package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequestDTO(

        @NotBlank(message = "Label is required")
        @Size(max = 100, message = "Label can be at most 100 characters")
        String label,

        @NotBlank(message = "Street Line 1 is required")
        @Size(max = 255, message = "Street Line 1 can be at most 255 characters")
        String streetLine1,

        @Size(max = 255, message = "Street Line 2 can be at most 255 characters")
        String streetLine2,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City can be at most 100 characters")
        String city,

        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State can be at most 100 characters")
        String state,

        @NotBlank(message = "Postal Code is required")
        @Size(max = 20, message = "Postal Code can be at most 20 characters")
        String postalCode,

        @NotBlank(message = "Country Code is required")
        @Size(min = 2, max = 2, message = "Country Code must be 2 characters")
        String countryCode
) {}
