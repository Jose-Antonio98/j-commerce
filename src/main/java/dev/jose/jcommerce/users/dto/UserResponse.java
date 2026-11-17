package dev.jose.jcommerce.users.dto;

import dev.jose.jcommerce.users.domain.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "UserResponse", description = "Dados públicos de um usuário; não inclui a senha.")
public record UserResponse(
        @Schema(description = "Identificador do usuário")
        UUID id,
        @Schema(description = "Email do usuário", example = "cliente@example.com")
        String email,
        @Schema(description = "Perfil de acesso")
        Role role,
        @Schema(description = "Indica se a conta está ativa")
        Boolean ative,
        @Schema(description = "Data e hora de criação")
        LocalDateTime createdAt,
        @Schema(description = "Data e hora da última atualização")
        LocalDateTime updateAt
) {

}
