package com.manthau.userservice.feature.profile;

import com.manthau.userservice.feature.follow.FollowRepository;
import com.manthau.userservice.shared.cache.CacheService;
import com.manthau.userservice.shared.exception.DisplayNameAlreadyExistsException;
import com.manthau.userservice.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final CacheService cacheService;
    private final FollowRepository followRepository;
    private final NameTagGenerator nameTagGenerator;

    private static final int MAX_NAME_TAG_GENERATION_ATTEMPTS = 20;

    // =================== READ ===================

    // Public profile - cache-aside pattern
    // requesterId is nullable: null for anonymous users, populated for signed-in users.
    public UserProfileResponse getPublicProfile(String username, UUID requesterId) {
        UserProfileResponse cached = cacheService.getProfile(username);
        if (cached != null && requesterId == null) {
            // Use the cache only for anonymous users because signed-in users need accurate isFollowing data.
            log.debug("Cache hit for profile: {}", username);
            return cached;
        }

        UserProfile profile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        // Check whether the requester is following this profile.
        Boolean isFollowing = null;
        if (requesterId != null) {
            isFollowing = followRepository.existsByIdFollowerIdAndIdFollowingId(
                    requesterId, profile.getId());
        }

        UserProfileResponse response = userProfileMapper.toResponse(profile, isFollowing);

        // Cache responses only for anonymous users (isFollowing = null).
        if (requesterId == null) {
            cacheService.setProfile(username, response);
        }

        return response;
    }

    // Own profile - always read from the database, not the cache.
    public UserProfileResponse getMyProfile(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        // Banned users can still view their own profile.
        return userProfileMapper.toResponse(profile, null);
    }

    // =================== UPDATE ===================

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        UserProfile profile = findActiveUserOrThrow(userId);

        updateDisplayName(profile, request);
        userProfileMapper.updateEntity(profile, request);
        userProfileRepository.save(profile);

        // Invalidate the cache after updating the profile.
        cacheService.evictProfile(profile.getUsername());

        return userProfileMapper.toResponse(profile, null);
    }

    // =================== DELETE ===================

    @Transactional
    public void deleteUser(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        // Soft delete - change the status instead of deleting the row.
        profile.setStatus(UserStatus.DELETED);
        userProfileRepository.save(profile);

        cacheService.evictProfile(profile.getUsername());
        log.info("User deleted (soft): {}", profile.getUsername());
    }

    // =================== ADMIN ===================

    @Transactional
    public void banUser(UUID userId) {
        UserProfile profile = findActiveUserOrThrow(userId);
        profile.setStatus(UserStatus.BANNED);
        userProfileRepository.save(profile);

        cacheService.evictProfile(profile.getUsername());
        log.info("User banned: {}", profile.getUsername());
    }

    @Transactional
    public void unbanUser(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        if (profile.getStatus() != UserStatus.BANNED) {
            throw new IllegalArgumentException("User is not banned");
        }

        profile.setStatus(UserStatus.ACTIVE);
        userProfileRepository.save(profile);
        log.info("User unbanned: {}", profile.getUsername());
    }

    // =================== INTERNAL ===================

    // Internal service lookup - only returns active users.
    public UserProfile findActiveUserOrThrow(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        if (profile.getStatus() == UserStatus.BANNED) {
            throw new IllegalStateException("User account is banned");
        }
        if (profile.getStatus() == UserStatus.DELETED) {
            throw new UserNotFoundException(userId.toString());
        }

        return profile;
    }

    public UserProfile findByIdOrThrow(UUID userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
    }

    public UserProfile findByUsernameOrThrow(String username) {
        return userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    // Called when a UserRegisteredEvent is received from Auth Service.
    @Transactional
    public void createProfile(UUID userId, String username, String displayName) {
        if (userProfileRepository.existsById(userId)) {
            log.warn("Profile already exists for userId: {}", userId);
            return;
        }
        String normalizedDisplayName = normalizeDisplayName(firstNonBlank(displayName, username));
        String nameTag = generateUniqueNameTag(normalizedDisplayName);
        UserProfile profile = UserProfile.builder()
                .id(userId)
                .username(username)
                .displayName(normalizedDisplayName)
                .nameTag(nameTag)
                .status(UserStatus.ACTIVE)
                .build();
        userProfileRepository.save(profile);
        log.info("Created profile for user: {}", username);
    }

    private void updateDisplayName(UserProfile profile, UpdateProfileRequest request) {
        if (request.getDisplayName() == null && request.getNameTag() == null) {
            return;
        }

        String displayName = request.getDisplayName() != null
                ? normalizeDisplayName(request.getDisplayName())
                : profile.getDisplayName();
        String nameTag = request.getNameTag() != null
                ? normalizeNameTag(request.getNameTag())
                : profile.getNameTag();

        if (nameTag == null || nameTag.isBlank()) {
            nameTag = generateUniqueNameTag(displayName);
        }

        if (userProfileRepository.existsByDisplayNameAndNameTagAndIdNot(displayName, nameTag, profile.getId())) {
            throw new DisplayNameAlreadyExistsException(displayName, nameTag);
        }

        profile.setDisplayName(displayName);
        profile.setNameTag(nameTag);
    }

    private String generateUniqueNameTag(String displayName) {
        for (int i = 0; i < MAX_NAME_TAG_GENERATION_ATTEMPTS; i++) {
            String nameTag = nameTagGenerator.generate();
            if (!userProfileRepository.existsByDisplayNameAndNameTag(displayName, nameTag)) {
                return nameTag;
            }
        }
        throw new IllegalStateException("Unable to generate unique name tag");
    }

    private String normalizeDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name must not be blank");
        }
        String normalized = displayName.trim();
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("Display name must not exceed 100 characters");
        }
        return normalized;
    }

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        return fallback;
    }

    private String normalizeNameTag(String nameTag) {
        String normalized = nameTag.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9]{6}")) {
            throw new IllegalArgumentException("Name tag must be exactly 6 letters or digits");
        }
        return normalized;
    }
}
