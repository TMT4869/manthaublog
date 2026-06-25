package com.manthau.postservice.post.event;

import java.util.UUID;

public record ViewCountUpdatedEvent(UUID postId, long viewCount) {}
