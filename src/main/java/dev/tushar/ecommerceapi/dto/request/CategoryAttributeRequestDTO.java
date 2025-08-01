package dev.tushar.ecommerceapi.dto.request;

import dev.tushar.ecommerceapi.model.AttributeType;
import jakarta.validation.constraints.NotNull;

public record CategoryAttributeRequestDTO(
        @NotNull(message = "Attribute ID is required")
        Long attributeId,

        @NotNull(message = "Attribute type is required")
        AttributeType attributeType,

        Long optionSetId // We need this only in case if attributeType is ENUM
) {}