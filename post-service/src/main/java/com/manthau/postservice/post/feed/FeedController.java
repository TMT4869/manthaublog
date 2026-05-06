package com.manthau.postservice.post.feed;

import com.manthau.postservice.post.list.PostSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedHandler handler;

    @GetMapping("/api/posts/feed")
    public Page<PostSummaryDto> feed(
            @RequestParam List<UUID> following,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return handler.handle(following, page, size);
    }
}
