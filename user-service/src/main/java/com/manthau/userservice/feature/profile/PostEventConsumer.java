package com.manthau.userservice.feature.profile;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.manthau.userservice.shared.cache.CacheService;
import com.manthau.userservice.shared.config.RabbitMQConfig;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostEventConsumer {

    private final UserProfileRepository userProfileRepository;
    private final CacheService cacheService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration IDEMPOTENCY_TTL = Duration.ofDays(7);

    @RabbitListener(queues = RabbitMQConfig.POST_APPROVED_QUEUE)
    public void onPostApproved(PostApprovedEvent event) {
        if (!markProcessed("post.approved", event.getPostId())) return;
        userProfileRepository.incrementPostsCount(event.getAuthorId());
        cacheService.evictProfile(event.getAuthorUsername());
    }

    @RabbitListener(queues = RabbitMQConfig.POST_ARCHIVED_QUEUE)
    public void onPostArchived(PostArchivedEvent event) {
        if (!markProcessed("post.archived", event.getPostId())) return;
        userProfileRepository.decrementPostsCount(event.getAuthorId());
        cacheService.evictProfile(event.getAuthorUsername());
    }

    private boolean markProcessed(String eventType, UUID entityId) {
        String key = "processed:" + eventType + ":" + entityId;
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", IDEMPOTENCY_TTL);
        return Boolean.TRUE.equals(isNew);
    }
}
