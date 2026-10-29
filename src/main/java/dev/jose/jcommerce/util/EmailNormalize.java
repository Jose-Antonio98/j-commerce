package dev.jose.jcommerce.util;

import java.util.Locale;

public class EmailNormalize {

    public static String normalizeEmail(String email) {
        return email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }
}
