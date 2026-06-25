package com.manthau.postservice.post.internal;

import com.manthau.postservice.post.domain.Post;
import com.manthau.postservice.post.domain.PostRepository;
import com.manthau.postservice.post.domain.PostStatus;
import com.manthau.postservice.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalGetPostHandler {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public InternalPostDto handle(UUID postId) {
        Post post = postRepository.findById(postId)
                .filter(p -> p.getStatus() == PostStatus.published)
                .orElseThrow(() -> new NotFoundException("Post not found or not published"));
        return InternalPostDto.from(post);
    }
}
