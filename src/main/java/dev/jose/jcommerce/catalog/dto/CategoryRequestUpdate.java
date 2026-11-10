package dev.jose.jcommerce.catalog.dto;

import dev.jose.jcommerce.util.NameNormalize;
import dev.jose.jcommerce.util.SlugNormalize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequestUpdate(

        @Size(min = 3, max = 255, message = "O nome deve ter no máximo 255 caracteres.")
        String name,

        @Size(min = 3, max = 255, message = "O slug deve conter entre 3 a 255 caracteres.")
        String slug


) {

    public CategoryRequestUpdate {
        name = NameNormalize.normalizeName(name);
        slug = SlugNormalize.normalizeSlug(slug);
    }


}
