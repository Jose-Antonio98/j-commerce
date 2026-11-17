package dev.jose.jcommerce.catalog.dto;

import dev.jose.jcommerce.util.NameNormalize;
import dev.jose.jcommerce.util.SlugNormalize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CategoryRequest", description = "Dados para cadastrar uma categoria.")
public record CategoryRequest(

        @Schema(description = "Nome da categoria", example = "Café da manhã")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 3, max = 255, message = "O nome deve conter entre 3 a 255 caracteres.")
        String name,

        @Schema(description = "Slug da categoria; será normalizado", example = "cafe-da-manha")
        @NotBlank(message = "O slug é obrigatório.")
        @Size(min = 3, max = 255, message = "O slug deve conter entre 3 a 255 caracteres.")
        String slug
) {

    public CategoryRequest {
        name = NameNormalize.normalizeName(name);
        slug = SlugNormalize.normalizeSlug(slug);
    }
}
