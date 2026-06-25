package com.manthau.postservice.post.archive;

import com.manthau.postservice.post.domain.Post;
import com.manthau.postservice.post.domain.PostRepository;
import com.manthau.postservice.post.domain.PostStatus;
import com.manthau.postservice.post.event.PostEventPublisher;
import com.manthau.postservice.shared.exception.BadRequestException;
import com.manthau.postservice.shared.exception.ForbiddenException;
import com.manthau.postservice.shared.exception.NotFoundException;
import com.manthau.postservice.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArchivePostHandler {

    private final PostRepository postRepository;
    private final PostEventPublisher eventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public void handle(UUID postId, UserPrincipal user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (!post.getAuthorId().equals(user.userId())) {
            throw new ForbiddenException("Not the post owner");
        }
        if (post.getStatus() != PostStatus.published) {
            throw new BadRequestException("Only published posts can be archived");
        }

        post.setStatus(PostStatus.archived);
        postRepository.save(post);
        redisTemplate.delete("post:" + postId);
        eventPublisher.publishArchived(postId);
    }
}
