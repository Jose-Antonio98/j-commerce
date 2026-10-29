package dev.jose.jcommerce.auth.service;

import dev.jose.jcommerce.auth.dto.LoginRequest;
import dev.jose.jcommerce.config.SecurityConfig;
import dev.jose.jcommerce.users.domain.User;
import dev.jose.jcommerce.users.domain.enums.Role;
import dev.jose.jcommerce.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@SpringJUnitConfig(AuthAuthenticationIntegrationTest.TestConfiguration.class)
class AuthAuthenticationIntegrationTest {

    private static final String EMAIL = "cliente@example.com";
    private static final String PASSWORD = "plainPassword";

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        reset(userRepository);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(createUser(true)));
    }

    @Test
    void authenticatesWithStoredPasswordHashAndLoadsRole() {
        Authentication result = authService.authenticate(
                new LoginRequest(" CLIENTE@EXAMPLE.COM ", PASSWORD));

        assertTrue(result.isAuthenticated());
        assertEquals(EMAIL, result.getName());
        assertInstanceOf(UserDetails.class, result.getPrincipal());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_CUSTOMER".equals(authority.getAuthority())));
    }

    @Test
    void rejectsIncorrectPassword() {
        assertThrows(BadCredentialsException.class,
                () -> authService.authenticate(new LoginRequest(EMAIL, "wrongPassword")));
    }

    @Test
    void rejectsUnknownUserWithGenericBadCredentialsResponse() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authService.authenticate(new LoginRequest(EMAIL, PASSWORD)));
    }

    @Test
    void rejectsDisabledUser() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(createUser(false)));

        assertThrows(DisabledException.class,
                () -> authService.authenticate(new LoginRequest(EMAIL, PASSWORD)));
    }

    private User createUser(boolean active) {
        var user = new User();
        user.setEmail(EMAIL);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(Role.CUSTOMER);
        user.setActive(active);
        return user;
    }

    @Configuration
    @Import({SecurityConfig.class, CustomUserDetailsService.class, AuthService.class})
    static class TestConfiguration {

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }
    }
}
