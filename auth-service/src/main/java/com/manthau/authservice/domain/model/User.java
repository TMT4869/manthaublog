package com.manthau.authservice.domain.model;

import com.manthau.authservice.domain.enums.AuthProvider;
import com.manthau.authservice.domain.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class User {
    private UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private AuthProvider provider;
    private String providerId;

    @Builder.Default
    private boolean verified = false;

    @Builder.Default
    private UserRole role = UserRole.USER;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isOAuthUser() {
        return provider != AuthProvider.LOCAL;
    }
}
