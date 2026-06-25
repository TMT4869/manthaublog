package com.manthau.authservice.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Getter
@Setter
@Builder
public class RefreshToken {
    private UUID id;
    private UUID userId;
    private String tokenHash;
    private LocalDateTime expiresAt;

    @Builder.Default
    private boolean revoked = false;

    private LocalDateTime createdAt;

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now(ZoneOffset.UTC));
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}
