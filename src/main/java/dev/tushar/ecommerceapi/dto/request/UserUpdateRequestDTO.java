package dev.tushar.ecommerceapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDTO {

    @Length(max = 50, message = "Last name must be at most 50 characters")
    @NotBlank(message = "First name is required")
    private String firstName;

    @Length(max = 50, message = "Last name must be at most 50 characters")
    private String lastName;

    @Length(max = 12, message = "Phone number must be at most 12 characters")
    private String phoneNumber;
}

//TODO: Need to Fix validation