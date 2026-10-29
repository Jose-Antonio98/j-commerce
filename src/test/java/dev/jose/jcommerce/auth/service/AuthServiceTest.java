package dev.jose.jcommerce.auth.service;

import dev.jose.jcommerce.auth.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticatesCredentialsAndReturnsAuthentication() {
        var request = new LoginRequest(" CLIENTE@EXAMPLE.COM ", "plainPassword");
        Authentication authenticated = org.mockito.Mockito.mock(Authentication.class);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authenticated);

        Authentication result = authService.authenticate(request);

        ArgumentCaptor<Authentication> credentialsCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(credentialsCaptor.capture());
        assertEquals(UsernamePasswordAuthenticationToken.class, credentialsCaptor.getValue().getClass());
        assertEquals("cliente@example.com", credentialsCaptor.getValue().getPrincipal());
        assertEquals("plainPassword", credentialsCaptor.getValue().getCredentials());
        assertSame(authenticated, result);
    }

    @Test
    void propagatesBadCredentialsWhenAuthenticationFails() {
        var request = new LoginRequest("cliente@example.com", "wrongPassword");
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(request));

        verify(authenticationManager).authenticate(any(Authentication.class));
    }
}
