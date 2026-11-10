package dev.jose.jcommerce.catalog.dto;

import dev.jose.jcommerce.util.NameNormalize;
import dev.jose.jcommerce.util.SlugNormalize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 3, max = 255, message = "O nome deve conter entre 3 a 255 caracteres.")
        String name,

        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 3, max = 255, message = "O nome deve conter entre 3 a 255 caracteres.")
        String slug,

        String description,

        @PositiveOrZero
        BigDecimal price,

        @PositiveOrZero
        Integer quantity,

        @Size(min = 3, max = 255, message = "O nome deve conter entre 3 a 255 caracteres.")
        String image,

        @NotNull(message = "A categoria é obrigatoria.")
        UUID categoryId
) {

    public ProductRequest{
        NameNormalize.normalizeName(name);
        SlugNormalize.normalizeSlug(slug);
    }
}
