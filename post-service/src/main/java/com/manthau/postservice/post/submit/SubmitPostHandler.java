package com.manthau.postservice.post.submit;

import com.manthau.postservice.post.domain.Post;
import com.manthau.postservice.post.domain.PostRepository;
import com.manthau.postservice.post.domain.PostStatus;
import com.manthau.postservice.post.event.PostEventPublisher;
import com.manthau.postservice.shared.exception.BadRequestException;
import com.manthau.postservice.shared.exception.ForbiddenException;
import com.manthau.postservice.shared.exception.NotFoundException;
import com.manthau.postservice.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmitPostHandler {

    private static final Set<PostStatus> SUBMITTABLE = Set.of(
            PostStatus.draft, PostStatus.rejected, PostStatus.archived
    );

    private final PostRepository postRepository;
    private final PostEventPublisher eventPublisher;

    @Transactional
    public void handle(UUID postId, UserPrincipal user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (!post.getAuthorId().equals(user.userId())) {
            throw new ForbiddenException("Not the post owner");
        }
        if (!SUBMITTABLE.contains(post.getStatus())) {
            throw new BadRequestException("Cannot submit post with status: " + post.getStatus());
        }

        post.setStatus(PostStatus.pending_review);
        post.setSubmittedAt(Instant.now());
        post.setRejectionReason(null);
        postRepository.save(post);
        eventPublisher.publishSubmitted(postId, user.userId());
    }
}
