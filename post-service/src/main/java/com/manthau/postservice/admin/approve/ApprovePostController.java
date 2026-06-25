package com.manthau.postservice.admin.approve;

import com.manthau.postservice.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ApprovePostController {

    private final ApprovePostHandler handler;

    @PostMapping("/api/admin/posts/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable UUID id) {
        handler.handle(id, UserPrincipal.current());
        return ResponseEntity.noContent().build();
    }
}
