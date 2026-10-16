package dev.jose.jcommerce.users.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserTest {

    @Test
    void normalizesEmailWhenSet() {
        User user = new User();

        user.setEmail("  Jose.Exemplo+loja@Example.COM  ");

        assertEquals("jose.exemplo+loja@example.com", user.getEmail());
    }

    @Test
    void keepsNullEmailNullForValidation() {
        User user = new User();

        user.setEmail(null);

        assertNull(user.getEmail());
    }
}
