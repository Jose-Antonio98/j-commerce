package dev.jose.jcommerce.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(name = "ProductResponse", description = "Dados retornados para um produto.")
public record ProductResponse(

        @Schema(description = "Identificador do produto")
        UUID id,
        @Schema(description = "Nome do produto", example = "Cafeteira")
        String name,
        @Schema(description = "Slug único do produto", example = "cafeteira")
        String slug,
        @Schema(description = "Descrição do produto")
        String description,
        @Schema(description = "Preço do produto", example = "129.90")
        BigDecimal price,
        @Schema(description = "Quantidade disponível")
        Integer quantity,
        @Schema(description = "Indica se o produto está ativo")
        Boolean active,
        @Schema(description = "URL da imagem")
        String image,
        @Schema(description = "ID da categoria associada")
        UUID category,
        @Schema(description = "Nome da categoria associada")
        String categoryName
) {
}
