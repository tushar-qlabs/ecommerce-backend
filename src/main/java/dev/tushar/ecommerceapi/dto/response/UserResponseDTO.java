package dev.tushar.ecommerceapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponseDTO(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phoneNumber
) {}