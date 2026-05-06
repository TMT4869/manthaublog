package com.manthau.postservice.admin.reject;

import com.manthau.postservice.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RejectPostController {

    private final RejectPostHandler handler;

    @PostMapping("/api/admin/posts/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable UUID id, @Valid @RequestBody RejectPostRequest req) {
        handler.handle(id, req, UserPrincipal.current());
        return ResponseEntity.noContent().build();
    }
}
