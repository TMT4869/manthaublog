package com.manthau.commentservice.comment.update;

import jakarta.validation.constraints.NotBlank;

public record UpdateCommentRequest(@NotBlank String content) {}
