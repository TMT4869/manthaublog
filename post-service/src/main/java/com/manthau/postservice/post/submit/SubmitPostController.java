package com.manthau.postservice.post.submit;

import com.manthau.postservice.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SubmitPostController {

    private final SubmitPostHandler handler;

    @PostMapping("/api/posts/{id}/submit")
    public ResponseEntity<Void> submit(@PathVariable UUID id) {
        handler.handle(id, UserPrincipal.current());
        return ResponseEntity.noContent().build();
    }
}
