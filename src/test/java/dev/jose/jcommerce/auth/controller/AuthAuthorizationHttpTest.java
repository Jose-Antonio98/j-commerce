package dev.jose.jcommerce.auth.controller;

import dev.jose.jcommerce.auth.service.AuthService;
import dev.jose.jcommerce.auth.service.CustomUserDetailsService;
import dev.jose.jcommerce.config.JwtConfiguration;
import dev.jose.jcommerce.config.SecurityConfig;
import dev.jose.jcommerce.users.controller.UserController;
import dev.jose.jcommerce.users.domain.enums.Role;
import dev.jose.jcommerce.users.dto.UserResponse;
import dev.jose.jcommerce.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        properties = {
                "app.security.jwt.secret-base64=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
                "app.security.jwt.access-token-ttl=15m"
        }
)
@Import({SecurityConfig.class, JwtConfiguration.class})
@EnableWebSecurity
class AuthAuthorizationHttpTest {

    private static final String ADMIN_USER_URL = "/auth/admin/users/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get(ADMIN_USER_URL + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsRequestWithInvalidToken() throws Exception {
        mockMvc.perform(get(ADMIN_USER_URL + UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsRequestWithExpiredToken() throws Exception {
        Instant now = Instant.now();
        String expiredToken = createToken("ADMIN", now.minusSeconds(600), now.minusSeconds(300));

        mockMvc.perform(get(ADMIN_USER_URL + UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbidsCustomerFromAccessingAdminRoute() throws Exception {
        String customerToken = createToken("CUSTOMER", Instant.now(), Instant.now().plusSeconds(300));

        mockMvc.perform(get(ADMIN_USER_URL + UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAdminToAccessAdminRoute() throws Exception {
        UUID userId = UUID.randomUUID();
        var response = new UserResponse(
                userId,
                "customer@example.com",
                Role.CUSTOMER,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(userService.findUserById(userId)).thenReturn(response);
        String adminToken = createToken("ADMIN", Instant.now(), Instant.now().plusSeconds(300));

        mockMvc.perform(get(ADMIN_USER_URL + userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk());

        verify(userService).findUserById(userId);
    }

    private String createToken(String role, Instant issuedAt, Instant expiresAt) {
        var claims = JwtClaimsSet.builder()
                .issuer("j-commerce")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject("test@example.com")
                .claim("roles", List.of(role))
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
