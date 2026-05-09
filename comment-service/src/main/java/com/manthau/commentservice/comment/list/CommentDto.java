package com.manthau.commentservice.comment.list;

import com.manthau.commentservice.comment.domain.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CommentDto(
        UUID id,
        UUID postId,
        UUID authorId,
        UUID parentId,
        String content,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentDto> replies
) {
    public static CommentDto from(Comment c) {
        return new CommentDto(
                c.getId(),
                c.getPostId(),
                c.getAuthorId(),
                c.getParentId(),
                c.isDeleted() ? null : c.getContent(),
                c.isDeleted(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                new ArrayList<>()
        );
    }
}
