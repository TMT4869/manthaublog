package com.manthau.postservice.category.posts;

import com.manthau.postservice.post.list.PostSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CategoryPostsController {

    private final CategoryPostsHandler handler;

    @GetMapping("/api/categories/{slug}/posts")
    public Page<PostSummaryDto> getPostsByCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return handler.handle(slug, page, size);
    }
}
