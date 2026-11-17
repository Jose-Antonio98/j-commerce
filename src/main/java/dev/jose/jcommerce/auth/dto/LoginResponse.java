package dev.jose.jcommerce.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(name = "LoginResponse", description = "Token de acesso emitido após autenticação.")
public record LoginResponse(
        @Schema(description = "Token JWT de acesso")
        String accessToken,
        @Schema(description = "Tipo de token", example = "Bearer")
        String tokenType
) {

}
