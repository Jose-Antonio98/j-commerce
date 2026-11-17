package dev.jose.jcommerce.catalog.dto;

import dev.jose.jcommerce.util.NameNormalize;
import dev.jose.jcommerce.util.SlugNormalize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(name = "ProductRequestUpdate", description = "Campos opcionais para atualização parcial de um produto.")
public record ProductRequestUpdate (
        @Schema(description = "Novo nome do produto", example = "Cafeteira")
        @Size(min = 3, max = 255, message = "O nome deve conter entre 3 a 255 caracteres.")
        String name,

        @Schema(description = "Novo slug do produto", example = "cafeteira")
        @Size(min = 3, max = 255, message = "O nome deve conter entre 3 a 255 caracteres.")
        String slug,

        @Schema(description = "Nova descrição do produto")
        String description,

        @Schema(description = "Novo preço do produto", example = "129.90")
        @PositiveOrZero
        BigDecimal price,

        @Schema(description = "Nova quantidade disponível", example = "25")
        @PositiveOrZero
        Integer quantity,

        @Schema(description = "Nova URL da imagem")
        @Size(min = 3, max = 255, message = "O nome deve conter entre 3 a 255 caracteres.")
        String image,

        @Schema(description = "ID da nova categoria associada")
        UUID categoryId
){
    public ProductRequestUpdate{
        name = NameNormalize.normalizeName(name);
        slug = SlugNormalize.normalizeSlug(slug);
    }
}
