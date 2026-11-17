package dev.jose.jcommerce.catalog.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductFilterRequest(
        String name,
        UUID categoryId,
        Boolean active,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
