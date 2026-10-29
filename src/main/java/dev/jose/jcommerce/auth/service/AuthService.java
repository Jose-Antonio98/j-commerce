package dev.jose.jcommerce.auth.service;

import dev.jose.jcommerce.auth.dto.LoginRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    public Authentication authenticate(LoginRequest request) {
        var credentials = new UsernamePasswordAuthenticationToken(
                request.email(), request.password()
        );

        return authenticationManager.authenticate(credentials);
    }

    public String login(LoginRequest request) {
        Authentication authentication = authenticate(request);
        return jwtTokenService.generateToken(authentication);
    }
}
