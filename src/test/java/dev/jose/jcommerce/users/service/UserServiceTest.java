package dev.jose.jcommerce.users.service;

import dev.jose.jcommerce.exception.ResourceAlreadyRegistered;
import dev.jose.jcommerce.users.domain.User;
import dev.jose.jcommerce.users.domain.enums.Role;
import dev.jose.jcommerce.users.dto.UserRequest;
import dev.jose.jcommerce.users.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createsCustomerAndStoresOnlyEncodedPassword() {
        UserRequest request = new UserRequest(" Customer@Example.com ", "plainPass123");
        when(userRepository.existsByEmail("customer@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPass123")).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = userService.createUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("customer@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPasswordHash());
        assertEquals(Role.CUSTOMER, savedUser.getRole());
        assertEquals("customer@example.com", response.email());
        assertEquals(Role.CUSTOMER, response.role());
        verify(passwordEncoder).encode("plainPass123");
    }

    @Test
    void rejectsDuplicateEmailBeforeEncodingOrSaving() {
        UserRequest request = new UserRequest("customer@example.com", "plainPass123");
        when(userRepository.existsByEmail("customer@example.com")).thenReturn(true);

        assertThrows(ResourceAlreadyRegistered.class, () -> userService.createUser(request));

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void convertsEmailUniqueConstraintViolationToDuplicateResourceException() {
        UserRequest request = new UserRequest("customer@example.com", "plainPass123");
        ConstraintViolationException constraintViolation = new ConstraintViolationException(
                "duplicate email", new SQLException(), "users_email_key");
        when(userRepository.existsByEmail("customer@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPass123")).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("unique email", constraintViolation));

        ResourceAlreadyRegistered exception = assertThrows(
                ResourceAlreadyRegistered.class,
                () -> userService.createUser(request));

        assertTrue(exception.getMessage().contains("usuário"));
    }
}
