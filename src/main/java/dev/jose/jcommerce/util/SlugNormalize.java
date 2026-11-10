package dev.jose.jcommerce.util;

import java.text.Normalizer;
import java.util.Locale;

public class SlugNormalize {

    public static String normalizeSlug(String slug) {
        if (slug == null) {
            return null;
        }

        String normalized = Normalizer.normalize(slug.strip().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);

        return normalized.replaceAll("\\p{M}+", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
