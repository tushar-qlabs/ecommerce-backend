package dev.tushar.ecommerceapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.tushar.ecommerceapi.model.AttributeType;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryAttributeResponseDTO(
        Long attributeId,
        String name,
        AttributeType type,
        List<String> options
) {}