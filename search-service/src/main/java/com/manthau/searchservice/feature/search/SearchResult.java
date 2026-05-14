package com.manthau.searchservice.feature.search;

import com.manthau.searchservice.feature.document.PostDocument;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public record SearchResult(
        String id,
        String title,
        String excerpt,
        String slug,
        String language,
        List<String> tags,
        String authorId,
        String authorName,
        String authorNameTag,
        String authorFullDisplayName,
        Instant publishedAt
) implements Serializable {

    public static SearchResult from(PostDocument doc) {
        return new SearchResult(
                doc.getId(),
                doc.getTitle(),
                doc.getExcerpt(),
                doc.getSlug(),
                doc.getLanguage(),
                doc.getTags(),
                doc.getAuthorId(),
                doc.getAuthorName(),
                doc.getAuthorNameTag(),
                doc.getAuthorFullDisplayName(),
                doc.getPublishedAt()
        );
    }
}
