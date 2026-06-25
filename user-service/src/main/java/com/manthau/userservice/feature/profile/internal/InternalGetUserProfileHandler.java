package com.manthau.userservice.feature.profile.internal;

import com.manthau.userservice.feature.profile.UserProfile;
import com.manthau.userservice.feature.profile.UserProfileRepository;
import com.manthau.userservice.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalGetUserProfileHandler {

    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public InternalUserProfileDto handle(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        return InternalUserProfileDto.from(profile);
    }
}
