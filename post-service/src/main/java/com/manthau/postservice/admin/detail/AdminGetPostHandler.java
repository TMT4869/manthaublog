package com.manthau.postservice.admin.detail;

import com.manthau.postservice.post.domain.Post;
import com.manthau.postservice.post.domain.PostRepository;
import com.manthau.postservice.post.get.PostDetailDto;
import com.manthau.postservice.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminGetPostHandler {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public PostDetailDto handle(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        return PostDetailDto.from(post);
    }
}
