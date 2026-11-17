package dev.jose.jcommerce.auth.dto;

import dev.jose.jcommerce.util.EmailNormalize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "Credenciais para autenticação.")
public record LoginRequest(
        @Schema(description = "Email cadastrado", example = "cliente@example.com")
        @Email(message = "Informe um email valido!")
        @NotBlank
        String email,

        @Schema(description = "Senha da conta", example = "SenhaSegura123")
        @NotBlank
        String password
) {

    public LoginRequest {
        email = EmailNormalize.normalizeEmail(email);
    }


}
