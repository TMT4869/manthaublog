package com.manthau.commentservice.comment.create;

import com.manthau.commentservice.comment.list.CommentDto;
import com.manthau.commentservice.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CreateCommentController {

    private final CreateCommentHandler handler;

    @PostMapping("/api/comments")
    public ResponseEntity<CommentDto> create(@Valid @RequestBody CreateCommentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(handler.handle(req, UserPrincipal.current()));
    }
}
