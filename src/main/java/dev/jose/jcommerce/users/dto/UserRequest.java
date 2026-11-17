package dev.jose.jcommerce.users.dto;

import dev.jose.jcommerce.util.EmailNormalize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Schema(name = "UserRequest", description = "Dados necessários para cadastrar um usuário.")
public record UserRequest(
        @Schema(description = "Email do usuário", example = "cliente@example.com")
        @Email(message = "Informe um email valido!")
        @NotBlank
        String email,

        @Schema(description = "Senha com 8 a 20 caracteres", example = "SenhaSegura123")
        @NotBlank
        @Size(min = 8, max = 20)
        String password
) {
    public UserRequest {
        email = EmailNormalize.normalizeEmail(email);
    }


}