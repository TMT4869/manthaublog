package com.manthau.commentservice.comment.list;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ListCommentsController {

    private final ListCommentsHandler handler;

    @GetMapping("/api/comments")
    public List<CommentDto> list(@RequestParam UUID postId) {
        return handler.handle(postId);
    }
}
