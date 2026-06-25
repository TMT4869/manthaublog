package com.manthau.postservice.post.archive;

import com.manthau.postservice.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ArchivePostController {

    private final ArchivePostHandler handler;

    @DeleteMapping("/api/posts/{id}")
    public ResponseEntity<Void> archive(@PathVariable UUID id) {
        handler.handle(id, UserPrincipal.current());
        return ResponseEntity.noContent().build();
    }
}
