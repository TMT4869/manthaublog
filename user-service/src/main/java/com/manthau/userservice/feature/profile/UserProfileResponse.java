package com.manthau.userservice.feature.profile;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {
    private UUID id;
    private String username;
    private String displayName;
    private String nameTag;
    private String fullDisplayName;
    private String bio;
    private String avatarUrl;
    private String website;
    private String location;
    private int followersCount;
    private int followingCount;
    private int postsCount;
    private Boolean isFollowing;
    private LocalDateTime createdAt;
}
