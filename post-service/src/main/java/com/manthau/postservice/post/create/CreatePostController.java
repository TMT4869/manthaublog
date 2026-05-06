package com.manthau.postservice.post.create;

import com.manthau.postservice.post.get.PostDetailDto;
import com.manthau.postservice.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CreatePostController {

    private final CreatePostHandler handler;

    @PostMapping("/api/posts")
    public ResponseEntity<PostDetailDto> create(@Valid @RequestBody CreatePostRequest req) {
        PostDetailDto result = handler.handle(req, UserPrincipal.current());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
