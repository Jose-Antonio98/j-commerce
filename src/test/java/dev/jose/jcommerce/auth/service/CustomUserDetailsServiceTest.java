package dev.jose.jcommerce.auth.service;

import dev.jose.jcommerce.users.domain.User;
import dev.jose.jcommerce.users.domain.enums.Role;
import dev.jose.jcommerce.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void shouldReturnUserDetailsWhenUserExists() {
        var user = new User();
        user.setEmail("cliente@example.com");
        user.setPasswordHash("hash-da-senha");
        user.setRole(Role.CUSTOMER);
        user.setActive(true);

        when(userRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(" CLIENTE@EXAMPLE.COM ");

        assertEquals("cliente@example.com",  userDetails.getUsername());
        assertEquals("hash-da-senha", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority()
                        .equals("ROLE_CUSTOMER")));
    }

    @Test
    void shouldReturnUserDetailsWhenUserNotExists() {
        when(userRepository.findByEmail("cliente@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("cliente@example.com"));

    }

    @Test
    void shouldReturnUserDetailsWhenUserIsDisabled() {
        var user = new User();
        user.setEmail("cliente@example.com");
        user.setPasswordHash("hash-da-senha");
        user.setRole(Role.CUSTOMER);
        user.setActive(false);

        when(userRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(" CLIENTE@EXAMPLE.COM ");

        assertEquals("cliente@example.com",  userDetails.getUsername());
        assertEquals("hash-da-senha", userDetails.getPassword());
        assertFalse(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority()
                        .equals("ROLE_CUSTOMER")));
    }
}
