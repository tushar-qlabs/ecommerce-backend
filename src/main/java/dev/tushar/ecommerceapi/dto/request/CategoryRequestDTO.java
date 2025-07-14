package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CategoryRequestDTO(
        @NotBlank(message = "Category name is required")
        @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
        String name,

        Long parentCategoryId,

        // We can also validate the nested DTO this way!!!!
        // The error of this will be delegated to the parent BindingResult
        @Valid
        Set<CategoryAttributeRequestDTO> categoryAttributes
) {}