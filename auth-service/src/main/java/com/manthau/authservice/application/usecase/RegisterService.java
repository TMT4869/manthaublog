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
        if (userPort.existsByEmail(cmd.email())) {
            throw ApplicationException.conflict("Email already in use");
        }

        User user = User.builder()
                .email(cmd.email())
                .passwordHash(passwordHashPort.hash(cmd.rawPassword()))
                .provider(AuthProvider.LOCAL)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userPort.save(user);

        publishRegisteredEvent(user, displayNameFrom(cmd));

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

    private String displayNameFrom(RegisterCommand cmd) {
        return cmd.displayName() != null ? cmd.displayName() : cmd.email().split("@")[0];
    }
}
