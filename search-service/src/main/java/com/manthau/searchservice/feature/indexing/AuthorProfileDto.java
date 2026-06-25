package com.manthau.searchservice.feature.indexing;

public record AuthorProfileDto(
        String id,
        String displayName,
        String nameTag,
        String fullDisplayName
) {}
