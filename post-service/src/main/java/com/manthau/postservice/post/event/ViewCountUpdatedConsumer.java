package com.manthau.postservice.post.event;

import com.manthau.postservice.post.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewCountUpdatedConsumer {

    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = "post-service.view.count.updated")
    @Transactional
    public void consume(ViewCountUpdatedEvent event) {
        postRepository.findById(event.postId()).ifPresent(post -> {
            post.setViewCount(event.viewCount());
            postRepository.save(post);
            redisTemplate.delete("post:" + post.getId());
            log.debug("Updated view_count for post {} = {}", post.getId(), event.viewCount());
        });
    }
}
