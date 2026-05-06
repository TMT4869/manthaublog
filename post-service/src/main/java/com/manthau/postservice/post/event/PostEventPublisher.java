package com.manthau.postservice.post.event;

import com.manthau.postservice.shared.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostEventPublisher {

    private static final String KEY_POST_ID   = "postId";
    private static final String KEY_AUTHOR_ID = "authorId";
    private static final String KEY_REASON    = "reason";

    private final RabbitTemplate rabbitTemplate;

    public void publishViewTracked(PostViewEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.POST_VIEW_TRACKED, "#", event);
        log.debug("Published view.tracked for post {}", event.postId());
    }

    public void publishSubmitted(UUID postId, UUID authorId) {
        Map<String, Object> payload = Map.of(KEY_POST_ID, postId, KEY_AUTHOR_ID, authorId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.POST_SUBMITTED, "#", payload);
    }

    public void publishApproved(PostApprovedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.POST_APPROVED, "#", event);
    }

    public void publishRejected(UUID postId, UUID authorId, String reason) {
        Map<String, Object> payload = Map.of(KEY_POST_ID, postId, KEY_AUTHOR_ID, authorId, KEY_REASON, reason);
        rabbitTemplate.convertAndSend(RabbitMQConfig.POST_REJECTED, "#", payload);
    }

    public void publishArchived(UUID postId) {
        Map<String, Object> payload = Map.of(KEY_POST_ID, postId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.POST_ARCHIVED, "#", payload);
    }
}
