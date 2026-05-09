package com.manthau.searchservice.feature.indexing;

import java.time.Instant;
import java.util.List;

public record InternalPostDto(
        String id,
        String authorId,
        String title,
        String slug,
        String excerpt,
        String content,
        String language,
        List<String> tags,
        Instant publishedAt
) {}
