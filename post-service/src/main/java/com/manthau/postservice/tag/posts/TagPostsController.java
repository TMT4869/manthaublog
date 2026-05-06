package com.manthau.postservice.tag.posts;

import com.manthau.postservice.post.list.PostSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TagPostsController {

    private final TagPostsHandler handler;

    @GetMapping("/api/tags/{slug}/posts")
    public Page<PostSummaryDto> getPostsByTag(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return handler.handle(slug, page, size);
    }
}
