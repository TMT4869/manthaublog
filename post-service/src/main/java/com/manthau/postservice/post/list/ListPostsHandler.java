package com.manthau.postservice.post.list;

import com.manthau.postservice.post.domain.PostRepository;
import com.manthau.postservice.post.domain.PostStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListPostsHandler {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<PostSummaryDto> handle(String language, UUID authorId, String categorySlug, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return postRepository.findPublished(PostStatus.published, language, authorId, categorySlug, pageable)
                .map(PostSummaryDto::from);
    }
}
