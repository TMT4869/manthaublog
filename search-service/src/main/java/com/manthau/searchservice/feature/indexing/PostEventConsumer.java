package com.manthau.searchservice.feature.indexing;

import com.manthau.searchservice.shared.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostEventConsumer {

    private final PostIndexService indexService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration IDEMPOTENCY_TTL = Duration.ofDays(7);

    @RabbitListener(queues = RabbitMQConfig.POST_APPROVED_QUEUE)
    public void onPostApproved(PostApprovedEvent event) {
        if (!markProcessed("post.approved", event.postId().toString())) return;
        log.debug("Consuming post.approved for postId={}", event.postId());
        indexService.indexPost(event.postId());
    }

    @RabbitListener(queues = RabbitMQConfig.POST_REJECTED_QUEUE)
    public void onPostRejected(Map<String, Object> event) {
        String postId = String.valueOf(event.get("postId"));
        if (!markProcessed("post.rejected", postId)) return;
        log.debug("Consuming post.rejected for postId={}", postId);
        indexService.unindexPost(postId);
    }

    @RabbitListener(queues = RabbitMQConfig.POST_ARCHIVED_QUEUE)
    public void onPostArchived(Map<String, Object> event) {
        String postId = String.valueOf(event.get("postId"));
        if (!markProcessed("post.archived", postId)) return;
        log.debug("Consuming post.archived for postId={}", postId);
        indexService.unindexPost(postId);
    }

    private boolean markProcessed(String eventType, String entityId) {
        String key = "processed:" + eventType + ":" + entityId;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, "1", IDEMPOTENCY_TTL);
        return Boolean.TRUE.equals(isNew);
    }
}
