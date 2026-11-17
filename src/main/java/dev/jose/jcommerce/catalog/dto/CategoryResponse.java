package dev.jose.jcommerce.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "CategoryResponse", description = "Dados retornados para uma categoria.")
public record CategoryResponse(
        @Schema(description = "Identificador da categoria")
        UUID id,
        @Schema(description = "Nome da categoria", example = "Café da manhã")
        String name,
        @Schema(description = "Slug da categoria", example = "cafe-da-manha")
        String slug,
        @Schema(description = "Indica se a categoria está ativa")
        Boolean active
) {
}
