package dev.jose.jcommerce.catalog.dto;

import jakarta.validation.Validation;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogRequestValidationTest {

    @Test
    void categoryCreateRequestNormalizesNameAndSlugBeforeValidation() {
        var request = new CategoryRequest("  Café   da manhã  ", " Café da manhã! ");

        assertEquals("Café da manhã", request.name());
        assertEquals("cafe-da-manha", request.slug());
        assertTrue(validate(request).isEmpty());
    }

    @Test
    void categoryUpdateAllowsOmittedFieldsButRejectsShortProvidedValues() {
        var emptyUpdate = new CategoryRequestUpdate(null, null);
        assertTrue(validate(emptyUpdate).isEmpty());

        var invalidUpdate = new CategoryRequestUpdate("Ab", null);
        assertTrue(fieldsWithViolations(invalidUpdate).contains("name"));
    }

    @Test
    void productCreateRequestNormalizesNameAndSlugAndRequiresPriceQuantityAndCategory() {
        var request = new ProductRequest(
                "  Cafeteira  ",
                " Café teira ",
                null,
                BigDecimal.ZERO,
                0,
                null,
                UUID.randomUUID()
        );

        assertEquals("Cafeteira", request.name());
        assertEquals("cafe-teira", request.slug());
        assertTrue(validate(request).isEmpty());

        var missingRequiredValues = new ProductRequest(
                "Cafeteira",
                "cafeteira",
                null,
                null,
                null,
                null,
                null
        );
        Set<String> fields = fieldsWithViolations(missingRequiredValues);
        assertTrue(fields.contains("price"));
        assertTrue(fields.contains("quantity"));
        assertTrue(fields.contains("categoryId"));
    }

    @Test
    void productRequestsRejectNegativePriceAndQuantity() {
        var request = new ProductRequest(
                "Cafeteira",
                "cafeteira",
                null,
                new BigDecimal("-0.01"),
                -1,
                null,
                UUID.randomUUID()
        );

        Set<String> fields = fieldsWithViolations(request);
        assertTrue(fields.contains("price"));
        assertTrue(fields.contains("quantity"));
    }

    @Test
    void productUpdateNormalizesProvidedFieldsAndAllowsOmittedFields() {
        var request = new ProductRequestUpdate(
                "  Moedor   de café ",
                " Moedor de café ",
                null,
                null,
                null,
                null,
                null
        );

        assertEquals("Moedor de café", request.name());
        assertEquals("moedor-de-cafe", request.slug());
        assertTrue(validate(request).isEmpty());

        var omittedFields = new ProductRequestUpdate(null, null, null, null, null, null, null);
        assertFalse(validate(omittedFields).stream().anyMatch(violation ->
                violation.getPropertyPath().toString().equals("name")
                        || violation.getPropertyPath().toString().equals("slug")));
    }

    private static Set<ConstraintViolation<Object>> validate(Object request) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(request);
        }
    }

    private static Set<String> fieldsWithViolations(Object request) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(request).stream()
                    .map(violation -> violation.getPropertyPath().toString())
                    .collect(Collectors.toSet());
        }
    }
}
