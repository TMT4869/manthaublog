package com.manthau.postservice.admin.detail;

import com.manthau.postservice.post.get.PostDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AdminGetPostController {

    private final AdminGetPostHandler handler;

    @GetMapping("/api/admin/posts/{id}")
    public PostDetailDto getPost(@PathVariable UUID id) {
        return handler.handle(id);
    }
}
