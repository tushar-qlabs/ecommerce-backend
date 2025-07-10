package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequestDTO {

    @Email(message = "Please provide a valid email address")
    @NotBlank
    private String email;

    @Pattern(
            regexp = "^.{4,}$",
            message = "Password must be at lease 4 characters long"
    )
    @NotBlank
    private String password;
}