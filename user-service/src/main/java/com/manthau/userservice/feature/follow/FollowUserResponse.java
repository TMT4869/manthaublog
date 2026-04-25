package com.manthau.userservice.feature.follow;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class FollowUserResponse {
    private UUID id;
    private String username;
    private String displayName;
    private String avatarUrl;
    private boolean isFollowing;
}