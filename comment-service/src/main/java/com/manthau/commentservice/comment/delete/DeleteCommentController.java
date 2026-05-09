package com.manthau.commentservice.comment.delete;

import com.manthau.commentservice.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DeleteCommentController {

    private final DeleteCommentHandler handler;

    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        handler.handle(id, UserPrincipal.current());
        return ResponseEntity.noContent().build();
    }
}
