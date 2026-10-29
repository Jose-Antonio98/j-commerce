package dev.jose.jcommerce.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final Duration accessTokenTtl;

    public JwtTokenService(JwtEncoder jwtEncoder,
                           @Value("${app.security.jwt.access-token-ttl}") Duration accessTokenTtl) {
        this.jwtEncoder = jwtEncoder;
        this.accessTokenTtl = accessTokenTtl;
    }

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("j-commerce")
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenTtl))
                .subject(authentication.getName())
                .claim("roles", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
                        .filter(authority -> authority.startsWith("ROLE_"))
                        .map(authority -> authority.substring("ROLE_".length()))
                        .toList())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
