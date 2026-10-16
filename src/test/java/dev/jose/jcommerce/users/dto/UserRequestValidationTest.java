package dev.jose.jcommerce.users.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRequestValidationTest {

    private static Validator validator;
    private static AutoCloseable validatorFactory;

    @BeforeAll
    static void setUpValidator() {
        var factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        validatorFactory = factory::close;
    }

    @AfterAll
    static void closeValidatorFactory() throws Exception {
        validatorFactory.close();
    }

    @Test
    void acceptsValidRegistrationFields() {
        UserRequest request = new UserRequest("customer@example.com", "plainPass123");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsInvalidEmail() {
        UserRequest request = new UserRequest("invalid-email", "plainPass123");

        Set<String> invalidFields = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertTrue(invalidFields.contains("email"));
    }

    @Test
    void rejectsMissingRequiredFields() {
        UserRequest request = new UserRequest(" ", "");

        Set<String> invalidFields = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertFalse(invalidFields.isEmpty());
        assertTrue(invalidFields.contains("email"));
        assertTrue(invalidFields.contains("password"));
    }
}
