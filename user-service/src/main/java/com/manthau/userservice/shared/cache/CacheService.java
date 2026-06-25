package com.manthau.userservice.shared.cache;

import com.manthau.userservice.feature.profile.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration PROFILE_TTL = Duration.ofMinutes(15);

    // =================== Profile Cache ===================

    public UserProfileResponse getProfile(String username) {
        try {
            Object raw = redisTemplate.opsForValue().get(profileKey(username));
            if (!(raw instanceof UserProfileResponse)) return null;
            return (UserProfileResponse) raw;
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

    // =================== Key patterns ===================

    private String profileKey(String username) {
        return "user:" + username + ":profile";
    }
}