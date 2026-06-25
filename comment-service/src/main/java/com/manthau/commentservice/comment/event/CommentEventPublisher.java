package com.manthau.commentservice.comment.event;

import com.manthau.commentservice.comment.domain.Comment;
import com.manthau.commentservice.shared.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishCommentCreated(Comment comment) {
        Map<String, Object> payload = Map.of(
                "commentId", comment.getId(),
                "postId", comment.getPostId(),
                "authorId", comment.getAuthorId(),
                "parentId", comment.getParentId() != null ? comment.getParentId() : ""
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.COMMENT_CREATED, "#", payload);
        log.debug("Published comment.created for comment {}", comment.getId());
    }
}
