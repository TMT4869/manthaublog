package com.manthau.userservice.shared.cache;

import com.manthau.userservice.feature.profile.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration PROFILE_TTL = Duration.ofMinutes(15);
    private static final Duration FOLLOW_COUNT_TTL = Duration.ofMinutes(5);

    // =================== Profile Cache ===================

    public UserProfileResponse getProfile(String username) {
        try {
            return (UserProfileResponse) redisTemplate.opsForValue().get(profileKey(username));
        } catch (Exception e) {
            log.warn("Cache get failed for profile: {}", username, e);
            return null; // Cache miss — fallback to DB
        }
    }

    public void setProfile(String username, UserProfileResponse response) {
        try {
            redisTemplate.opsForValue().set(profileKey(username), response, PROFILE_TTL);
        } catch (Exception e) {
            log.warn("Cache set failed for profile: {}", username, e);
        }
    }

    public void evictProfile(String username) {
        try {
            redisTemplate.delete(profileKey(username));
        } catch (Exception e) {
            log.warn("Cache evict failed for profile: {}", username, e);
        }
    }

    // =================== Follow Count Cache ===================

    public void evictFollowCount(UUID userId) {
        try {
            redisTemplate.delete(followCountKey(userId));
        } catch (Exception e) {
            log.warn("Cache evict failed for follow count: {}", userId, e);
        }
    }

    // =================== Key patterns ===================

    private String profileKey(String username) {
        return "user:" + username + ":profile";
    }

    private String followCountKey(UUID userId) {
        return "user:" + userId + ":follow_count";
    }
}