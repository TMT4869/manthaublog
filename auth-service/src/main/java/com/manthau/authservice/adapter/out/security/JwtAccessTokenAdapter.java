package com.manthau.authservice.adapter.out.security;

import com.manthau.authservice.application.port.out.AccessTokenPort;
import com.manthau.authservice.domain.model.User;
import com.manthau.authservice.infrastructure.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAccessTokenAdapter implements AccessTokenPort {

    private final AppProperties appProperties;

    @Override
    public String generate(User user) {
        long now = System.currentTimeMillis();
        long expMs = appProperties.getJwt().getAccessTokenExpiration();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .claim("email", user.getEmail())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expMs))
                .signWith(secretKey())
                .compact();
    }

    @Override
    public long expiresInSeconds() {
        return appProperties.getJwt().getAccessTokenExpiration() / 1000;
    }

    public Optional<Claims> validate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private SecretKey secretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(
                appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
