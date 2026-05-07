package com.manthau.authservice.adapter.out.security;

import com.manthau.authservice.adapter.out.persistence.entity.RefreshTokenEntity;
import com.manthau.authservice.adapter.out.persistence.jpa.RefreshTokenJpaRepository;
import com.manthau.authservice.application.port.out.RefreshTokenPort;
import com.manthau.authservice.infrastructure.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RefreshTokenAdapter implements RefreshTokenPort {

    private final RefreshTokenJpaRepository jpaRepository;
    private final StringRedisTemplate redisTemplate;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public String issue(UUID userId) {
        String rawToken = generateSecureToken();
        String tokenHash = hash(rawToken);
        long ttlMs = appProperties.getJwt().getRefreshTokenExpiration();

        jpaRepository.save(RefreshTokenEntity.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusSeconds(ttlMs / 1000))
                .build());

        redisTemplate.opsForValue().set(
                "session:" + userId, tokenHash, ttlMs, TimeUnit.MILLISECONDS);

        return rawToken;
    }

    @Override
    public Optional<UUID> validate(String rawToken) {
        String tokenHash = hash(rawToken);
        return jpaRepository.findByTokenHash(tokenHash)
                .filter(t -> !t.isRevoked() && t.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(RefreshTokenEntity::getUserId);
    }

    @Override
    @Transactional
    public void revokeAll(UUID userId) {
        jpaRepository.revokeAllByUserId(userId);
        redisTemplate.delete("session:" + userId);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            byte[] hashBytes = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
