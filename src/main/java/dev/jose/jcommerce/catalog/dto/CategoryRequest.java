package dev.jose.jcommerce.catalog.dto;

import dev.jose.jcommerce.util.NameNormalize;
import dev.jose.jcommerce.util.SlugNormalize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 3, max = 255, message = "O nome deve conter entre 3 a 255 caracteres.")
        String name,

        @NotBlank(message = "O slug é obrigatório.")
        @Size(min = 3, max = 255, message = "O slug deve conter entre 3 a 255 caracteres.")
        String slug
) {

    public CategoryRequest {
        name = NameNormalize.normalizeName(name);
        slug = SlugNormalize.normalizeSlug(slug);
    }
}
