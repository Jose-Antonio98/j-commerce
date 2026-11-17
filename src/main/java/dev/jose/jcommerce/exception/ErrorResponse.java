package dev.jose.jcommerce.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "ErrorResponse", description = "Formato padrão das respostas de erro da API.")
public record ErrorResponse(
        @Schema(description = "Data e hora do erro")
        LocalDateTime timestamp,
        @Schema(description = "Código HTTP", example = "400")
        Integer status,
        @Schema(description = "Descrição resumida do erro", example = "Erro de validação")
        String erro,
        @Schema(description = "Detalhes do erro")
        List<String> mensagens
) {
}
