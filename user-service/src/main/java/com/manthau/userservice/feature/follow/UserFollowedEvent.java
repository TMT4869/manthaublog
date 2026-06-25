package com.manthau.userservice.feature.follow;

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
public class UserFollowedEvent {
    private UUID followerId;
    private UUID followingId;
    private String followerUsername;
    private String followingUsername;
    private LocalDateTime followedAt;
}