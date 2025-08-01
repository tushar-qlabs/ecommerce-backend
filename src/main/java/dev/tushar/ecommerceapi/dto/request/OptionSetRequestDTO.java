package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OptionSetRequestDTO(
        @NotBlank(message = "Option set name is required")
        @Size(max = 100)
        String name,

        @NotEmpty(message = "Options list cannot be empty")
        List<String> options
) {}