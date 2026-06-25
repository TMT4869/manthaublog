package com.manthau.postservice.category.posts;

import com.manthau.postservice.category.domain.CategoryRepository;
import com.manthau.postservice.post.domain.Post;
import com.manthau.postservice.post.domain.PostRepository;
import com.manthau.postservice.post.domain.PostStatus;
import com.manthau.postservice.post.list.PostSummaryDto;
import com.manthau.postservice.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryPostsHandler {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<PostSummaryDto> handle(String categorySlug, int page, int size) {
        if (!categoryRepository.existsBySlug(categorySlug)) {
            throw new NotFoundException("Category not found");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Post::getPublishedAt).descending());
        return postRepository.findByCategorySlug(categorySlug, PostStatus.published, pageable)
                .map(PostSummaryDto::from);
    }
}
