package dev.jose.jcommerce.util;

public class NameNormalize {

    public static String normalizeName(String name) {
        return name == null ? null : name.strip().replaceAll("\\s+", " ");
    }
}
