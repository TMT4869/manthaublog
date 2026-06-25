package com.manthau.postservice.post.event;

import java.util.UUID;

public record PostViewEvent(UUID postId, UUID userId, String ipHash) {}
