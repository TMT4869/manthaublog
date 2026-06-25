package com.manthau.authservice.adapter.out.cache;

import com.manthau.authservice.adapter.out.email.JavaMailEmailAdapter;
import com.manthau.authservice.application.port.out.EmailVerificationPort;
import com.manthau.authservice.infrastructure.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisEmailVerificationAdapter implements EmailVerificationPort {

    private static final long TTL_HOURS = 24;
    private static final String KEY_PREFIX = "verify:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final AppProperties appProperties;
    private final JavaMailEmailAdapter emailAdapter;

    @Override
    public String createToken(UUID userId) {
        byte[] bytes = new byte[24];
        SECURE_RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redisTemplate.opsForValue().set(KEY_PREFIX + token, userId.toString(), Duration.ofHours(TTL_HOURS));
        return token;
    }

    @Override
    public Optional<UUID> consumeToken(String token) {
        String key = KEY_PREFIX + token;
        String userId = redisTemplate.opsForValue().get(key);
        if (userId == null) return Optional.empty();
        redisTemplate.delete(key);
        return Optional.of(UUID.fromString(userId));
    }

    @Override
    public void sendVerificationEmail(String toEmail, String verificationToken) {
        String url = appProperties.getBaseUrl() + "/auth/verify-email?token=" + verificationToken;
        emailAdapter.send(toEmail, url);
    }
}
