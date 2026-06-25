package com.manthau.userservice.feature.profile.internal;

import com.manthau.userservice.feature.profile.UserProfile;

public record InternalUserProfileDto(
        String id,
        String displayName,
        String nameTag,
        String fullDisplayName
) {
    public static InternalUserProfileDto from(UserProfile profile) {
        return new InternalUserProfileDto(
                profile.getId().toString(),
                profile.getDisplayName(),
                profile.getNameTag(),
                profile.getFullDisplayName()
        );
    }
}
