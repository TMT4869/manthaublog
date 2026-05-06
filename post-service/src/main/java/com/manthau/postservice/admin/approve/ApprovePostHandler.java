package com.manthau.postservice.admin.approve;

import com.manthau.postservice.admin.domain.PostReviewHistory;
import com.manthau.postservice.admin.domain.PostReviewHistoryRepository;
import com.manthau.postservice.admin.domain.ReviewAction;
import com.manthau.postservice.post.domain.Post;
import com.manthau.postservice.post.domain.PostRepository;
import com.manthau.postservice.post.domain.PostStatus;
import com.manthau.postservice.post.event.PostApprovedEvent;
import com.manthau.postservice.post.event.PostEventPublisher;
import com.manthau.postservice.shared.exception.BadRequestException;
import com.manthau.postservice.shared.exception.NotFoundException;
import com.manthau.postservice.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApprovePostHandler {

    private final PostRepository postRepository;
    private final PostReviewHistoryRepository historyRepository;
    private final PostEventPublisher eventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public void handle(UUID postId, UserPrincipal admin) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (post.getStatus() != PostStatus.pending_review) {
            throw new BadRequestException("Post is not pending review");
        }

        Instant now = Instant.now();
        post.setStatus(PostStatus.published);
        post.setReviewedAt(now);
        post.setReviewedBy(admin.userId());
        post.setPublishedAt(now);
        postRepository.save(post);

        historyRepository.save(PostReviewHistory.builder()
                .postId(postId)
                .adminId(admin.userId())
                .action(ReviewAction.approve)
                .build());

        redisTemplate.delete("post:" + postId);
        eventPublisher.publishApproved(new PostApprovedEvent(
                postId, post.getAuthorId(), post.getLanguage(), post.getSlug()
        ));
    }
}
