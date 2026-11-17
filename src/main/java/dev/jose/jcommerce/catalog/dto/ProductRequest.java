package dev.jose.jcommerce.catalog.dto;

import dev.jose.jcommerce.util.NameNormalize;
import dev.jose.jcommerce.util.SlugNormalize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(name = "ProductRequest", description = "Dados para cadastrar um produto.")
public record ProductRequest(

        @Schema(description = "Nome do produto", example = "Cafeteira")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 3, max = 255, message = "O nome deve conter entre 3 a 255 caracteres.")
        String name,

        @Schema(description = "Slug único do produto", example = "cafeteira")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 3, max = 255, message = "O nome deve conter entre 3 a 255 caracteres.")
        String slug,

        @Schema(description = "Descrição do produto", example = "Cafeteira elétrica de 12 xícaras")
        String description,

        @Schema(description = "Preço do produto", example = "129.90")
        @PositiveOrZero
        @NotNull
        BigDecimal price,

        @Schema(description = "Quantidade disponível em estoque", example = "25")
        @PositiveOrZero
        @NotNull
        Integer quantity,

        @Schema(description = "URL da imagem do produto", example = "https://example.com/images/cafeteira.jpg")
        @Size(min = 3, max = 255, message = "O nome deve conter entre 3 a 255 caracteres.")
        String image,

        @Schema(description = "ID da categoria associada", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "A categoria é obrigatoria.")
        UUID categoryId
) {

    public ProductRequest{
        name = NameNormalize.normalizeName(name);
        slug = SlugNormalize.normalizeSlug(slug);
    }
}
