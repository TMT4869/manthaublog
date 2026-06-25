package com.manthau.searchservice.feature.indexing;

import java.util.UUID;

public record PostApprovedEvent(UUID postId, UUID authorId, String language, String slug) {}
