package com.manthau.postservice.post.get;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GetPostController {

    private final GetPostHandler handler;

    @GetMapping("/api/posts/{slug}")
    public PostDetailDto getPost(@PathVariable String slug, HttpServletRequest request) {
        return handler.handle(slug, request);
    }
}
