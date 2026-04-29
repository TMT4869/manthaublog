package com.manthau.userservice.feature.profile;

import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    public UserProfileResponse toResponse(UserProfile entity) {
        return toResponse(entity, null);
    }

    public UserProfileResponse toResponse(UserProfile entity, Boolean isFollowing) {
        return UserProfileResponse.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .displayName(entity.getDisplayName())
                .bio(entity.getBio())
                .avatarUrl(entity.getAvatarUrl())
                .website(entity.getWebsite())
                .location(entity.getLocation())
                .followersCount(entity.getFollowersCount())
                .followingCount(entity.getFollowingCount())
                .postsCount(entity.getPostsCount())
                .isFollowing(isFollowing)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public void updateEntity(UserProfile entity, UpdateProfileRequest request) {
        if (request.getDisplayName() != null) entity.setDisplayName(request.getDisplayName());
        if (request.getBio() != null) entity.setBio(request.getBio());
        if (request.getAvatarUrl() != null) entity.setAvatarUrl(request.getAvatarUrl());
        if (request.getWebsite() != null) entity.setWebsite(request.getWebsite());
        if (request.getLocation() != null) entity.setLocation(request.getLocation());
    }
}