package com.manthau.postservice.tag.posts;

import com.manthau.postservice.post.domain.PostRepository;
import com.manthau.postservice.post.list.PostSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagPostsHandler {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<PostSummaryDto> handle(String tagSlug, int page, int size) {
        return postRepository.findByTagSlug(tagSlug, PageRequest.of(page, size))
                .map(PostSummaryDto::from);
    }
}
