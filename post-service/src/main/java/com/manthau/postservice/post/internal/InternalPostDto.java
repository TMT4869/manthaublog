package com.manthau.postservice.post.internal;

import com.manthau.postservice.post.domain.Post;

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
) {
    public static InternalPostDto from(Post post) {
        List<String> tagSlugs = post.getPostTags().stream()
                .map(pt -> pt.getTag().getSlug())
                .sorted()
                .toList();
        return new InternalPostDto(
                post.getId().toString(),
                post.getAuthorId().toString(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getContent(),
                post.getLanguage(),
                tagSlugs,
                post.getPublishedAt()
        );
    }
}
