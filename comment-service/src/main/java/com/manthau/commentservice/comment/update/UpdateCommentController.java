package com.manthau.commentservice.comment.update;

import com.manthau.commentservice.comment.list.CommentDto;
import com.manthau.commentservice.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UpdateCommentController {

    private final UpdateCommentHandler handler;

    @PutMapping("/api/comments/{id}")
    public CommentDto update(@PathVariable UUID id, @Valid @RequestBody UpdateCommentRequest req) {
        return handler.handle(id, req, UserPrincipal.current());
    }
}
