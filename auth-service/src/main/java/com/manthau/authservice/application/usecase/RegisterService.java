package com.manthau.authservice.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manthau.authservice.application.dto.AuthResult;
import com.manthau.authservice.application.dto.RegisterCommand;
import com.manthau.authservice.application.exception.ApplicationException;
import com.manthau.authservice.application.port.in.RegisterUseCase;
import com.manthau.authservice.application.port.out.*;
import com.manthau.authservice.domain.enums.AuthProvider;
import com.manthau.authservice.domain.model.OutboxEvent;
import com.manthau.authservice.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegisterService implements RegisterUseCase {

    private final UserPort userPort;
    private final OutboxEventPort outboxEventPort;
    private final AccessTokenPort accessTokenPort;
    private final RefreshTokenPort refreshTokenPort;
    private final PasswordHashPort passwordHashPort;
    private final EmailVerificationPort emailVerificationPort;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AuthResult register(RegisterCommand cmd) {
        String username = normalizeUsername(cmd.username());
        String email = normalizeEmail(cmd.email());

        if (userPort.existsByUsername(username)) {
            throw ApplicationException.conflict("Username already in use");
        }
        if (userPort.existsByEmail(email)) {
            throw ApplicationException.conflict("Email already in use");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordHashPort.hash(cmd.rawPassword()))
                .provider(AuthProvider.LOCAL)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userPort.save(user);

        publishRegisteredEvent(user, displayNameFrom(cmd, username));

        String verifyToken = emailVerificationPort.createToken(user.getId());
        emailVerificationPort.sendVerificationEmail(user.getEmail(), verifyToken);

        String accessToken = accessTokenPort.generate(user);
        String refreshToken = refreshTokenPort.issue(user.getId());

        return new AuthResult(accessToken, refreshToken, accessTokenPort.expiresInSeconds());
    }

    private void publishRegisteredEvent(User user, String displayName) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "userId", user.getId().toString(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "displayName", displayName
            ));
            outboxEventPort.save(OutboxEvent.builder()
                    .aggregateType("user")
                    .aggregateId(user.getId())
                    .eventType("user.registered")
                    .payload(payload)
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }
    }

    private String displayNameFrom(RegisterCommand cmd, String username) {
        return cmd.displayName() != null && !cmd.displayName().isBlank()
                ? cmd.displayName().trim()
                : username;
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
