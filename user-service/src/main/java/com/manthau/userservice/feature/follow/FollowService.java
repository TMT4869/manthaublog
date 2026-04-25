package com.manthau.userservice.feature.follow;

import com.manthau.userservice.feature.profile.UserProfile;
import com.manthau.userservice.feature.profile.UserService;
import com.manthau.userservice.shared.cache.CacheService;
import com.manthau.userservice.shared.exception.AlreadyFollowingException;
import com.manthau.userservice.feature.profile.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowRepository followRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserService userService;
    private final CacheService cacheService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Transactional
    public void follow(UUID followerId, String targetUsername) {
        UserProfile target = userService.findByUsernameOrThrow(targetUsername);

        // Không cho tự follow chính mình
        if (followerId.equals(target.getId())) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        // Kiểm tra đã follow chưa
        if (followRepository.existsByIdFollowerIdAndIdFollowingId(followerId, target.getId())) {
            throw new AlreadyFollowingException(targetUsername);
        }

        // Lưu quan hệ follow
        Follow follow = new Follow(new Follow.FollowId(followerId, target.getId()), null);
        followRepository.save(follow);

        // Cập nhật counter (denormalized)
        userProfileRepository.incrementFollowersCount(target.getId());
        userProfileRepository.incrementFollowingCount(followerId);

        // Invalidate cache
        cacheService.evictFollowCount(followerId);
        cacheService.evictFollowCount(target.getId());

        // Lấy follower info để publish event
        UserProfile follower = userService.findByIdOrThrow(followerId);

        // Publish event → Notification Service sẽ consume
        UserFollowedEvent event = UserFollowedEvent.builder()
                .followerId(followerId)
                .followingId(target.getId())
                .followerUsername(follower.getUsername())
                .followingUsername(target.getUsername())
                .followedAt(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(exchange, "user.followed", event);
        log.info("User {} followed {}", follower.getUsername(), targetUsername);
    }

    @Transactional
    public void unfollow(UUID followerId, String targetUsername) {
        UserProfile target = userService.findByUsernameOrThrow(targetUsername);

        if (!followRepository.existsByIdFollowerIdAndIdFollowingId(followerId, target.getId())) {
            return; // Idempotent — không throw nếu chưa follow
        }

        followRepository.deleteByIdFollowerIdAndIdFollowingId(followerId, target.getId());
        userProfileRepository.decrementFollowersCount(target.getId());
        userProfileRepository.decrementFollowingCount(followerId);

        cacheService.evictFollowCount(followerId);
        cacheService.evictFollowCount(target.getId());

        log.info("User {} unfollowed {}", followerId, targetUsername);
    }

    public boolean isFollowing(UUID followerId, UUID targetId) {
        return followRepository.existsByIdFollowerIdAndIdFollowingId(followerId, targetId);
    }

    public PagedResponse<FollowUserResponse> getFollowers(String username, int page, int size) {
        UserProfile target = userService.findByUsernameOrThrow(username);

        Page<FollowUserResponse> result = followRepository.findFollowersByUserId(
            target.getId(), PageRequest.of(page, size));

        return PagedResponse.<FollowUserResponse>builder()
            .content(result.getContent())
            .page(page)
            .size(size)
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .last(result.isLast())
            .build();
    }

    public PagedResponse<FollowUserResponse> getFollowing(String username, int page, int size) {
        UserProfile target = userService.findByUsernameOrThrow(username);
        Page<FollowUserResponse> result = followRepository.findFollowingByUserId(
            target.getId(), PageRequest.of(page, size));

        return PagedResponse.<FollowUserResponse>builder()
                .content(result.getContent())
                .page(page)
                .size(size)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }
}