package com.manthau.commentservice.comment.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCommentRequest(
        @NotNull UUID postId,
        UUID parentId,
        @NotBlank String content
) {}
