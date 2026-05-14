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

    // Public profile — cache-aside pattern
    // requesterId nullable: null nếu anonymous, có giá trị nếu đã login
    public UserProfileResponse getPublicProfile(String username, UUID requesterId) {
        UserProfileResponse cached = cacheService.getProfile(username);
        if (cached != null && requesterId == null) {
            // Chỉ dùng cache cho anonymous — logged-in user cần isFollowing chính xác
            log.debug("Cache hit for profile: {}", username);
            return cached;
        }

        UserProfile profile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        // Kiểm tra xem requester có đang follow không
        Boolean isFollowing = null;
        if (requesterId != null) {
            isFollowing = followRepository.existsByIdFollowerIdAndIdFollowingId(
                    requesterId, profile.getId());
        }

        UserProfileResponse response = userProfileMapper.toResponse(profile, isFollowing);

        // Chỉ cache response cho anonymous (isFollowing = null)
        if (requesterId == null) {
            cacheService.setProfile(username, response);
        }

        return response;
    }

    // Profile của chính mình — luôn lấy từ DB, không cache
    public UserProfileResponse getMyProfile(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        // Kiểm tra nếu bị ban thì vẫn cho xem profile của mình
        return userProfileMapper.toResponse(profile, null);
    }

    // =================== UPDATE ===================

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        UserProfile profile = findActiveUserOrThrow(userId);

        updateDisplayName(profile, request);
        userProfileMapper.updateEntity(profile, request);
        userProfileRepository.save(profile);

        // Invalidate cache sau khi update
        cacheService.evictProfile(profile.getUsername());

        return userProfileMapper.toResponse(profile, null);
    }

    // =================== DELETE ===================

    @Transactional
    public void deleteUser(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        // Soft delete — không xóa thật, chỉ đổi status
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

    // Dùng nội bộ trong service — chỉ lấy user ACTIVE
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

    // Gọi khi nhận UserRegisteredEvent từ Auth Service
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
