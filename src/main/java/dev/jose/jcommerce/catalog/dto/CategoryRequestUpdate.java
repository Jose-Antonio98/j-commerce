package dev.jose.jcommerce.catalog.dto;

import dev.jose.jcommerce.util.NameNormalize;
import dev.jose.jcommerce.util.SlugNormalize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "CategoryRequestUpdate", description = "Campos opcionais para atualização parcial de uma categoria.")
public record CategoryRequestUpdate(

        @Schema(description = "Novo nome da categoria", example = "Café da manhã")
        @Size(min = 3, max = 255, message = "O nome deve ter no máximo 255 caracteres.")
        String name,

        @Schema(description = "Novo slug da categoria; será normalizado", example = "cafe-da-manha")
        @Size(min = 3, max = 255, message = "O slug deve conter entre 3 a 255 caracteres.")
        String slug


) {

    public CategoryRequestUpdate {
        name = NameNormalize.normalizeName(name);
        slug = SlugNormalize.normalizeSlug(slug);
    }


}
