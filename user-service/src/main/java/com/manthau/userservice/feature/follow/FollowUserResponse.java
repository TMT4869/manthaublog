package com.manthau.userservice.feature.follow;

import lombok.Data;

import java.util.UUID;

@Data
public class FollowUserResponse {
    private UUID id;
    private String username;
    private String displayName;
    private String nameTag;
    private String fullDisplayName;
    private String avatarUrl;
    private boolean isFollowing;

    public FollowUserResponse(UUID id, String username, String displayName, String nameTag, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.nameTag = nameTag;
        this.fullDisplayName = nameTag == null || nameTag.isBlank()
                ? displayName
                : displayName + "#" + nameTag;
        this.avatarUrl = avatarUrl;
    }
}
