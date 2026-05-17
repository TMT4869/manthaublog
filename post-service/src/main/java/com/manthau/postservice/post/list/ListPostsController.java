package com.manthau.postservice.post.list;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ListPostsController {

    private final ListPostsHandler handler;

    @GetMapping("/api/posts")
    public Page<PostSummaryDto> list(
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) UUID authorId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return handler.handle(lang, authorId, category, page, size);
    }
}
