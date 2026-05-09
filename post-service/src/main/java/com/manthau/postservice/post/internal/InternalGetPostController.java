package com.manthau.postservice.post.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InternalGetPostController {

    private final InternalGetPostHandler handler;

    @GetMapping("/api/internal/posts/{id}")
    public InternalPostDto getPost(@PathVariable UUID id) {
        return handler.handle(id);
    }
}
