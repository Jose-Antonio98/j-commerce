package dev.jose.jcommerce.auth.dto;

import dev.jose.jcommerce.util.EmailNormalize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "Informe um email valido!")
        @NotBlank
        String email,

        @NotBlank
        String password
) {

    public LoginRequest {
        email = EmailNormalize.normalizeEmail(email);
    }


}
