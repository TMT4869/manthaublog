package com.manthau.postservice.post.update;

import com.manthau.postservice.post.get.PostDetailDto;
import com.manthau.postservice.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UpdatePostController {

    private final UpdatePostHandler handler;

    @PutMapping("/api/posts/{id}")
    public PostDetailDto update(@PathVariable UUID id, @Valid @RequestBody UpdatePostRequest req) {
        return handler.handle(id, req, UserPrincipal.current());
    }
}
