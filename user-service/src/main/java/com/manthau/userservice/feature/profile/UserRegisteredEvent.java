package com.manthau.userservice.feature.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {
    private UUID userId;
    private String username;
    private String email;
    private String displayName;
    private LocalDateTime registeredAt;
}