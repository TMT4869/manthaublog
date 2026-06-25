package com.manthau.postservice.post.event;

import java.util.UUID;

public record PostApprovedEvent(UUID postId, UUID authorId, String language, String slug) {}
