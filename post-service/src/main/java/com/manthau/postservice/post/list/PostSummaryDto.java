package com.manthau.postservice.post.list;

import com.manthau.postservice.category.dto.CategorySummaryDto;
import com.manthau.postservice.post.domain.Post;
import com.manthau.postservice.post.domain.PostStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostSummaryDto(
        UUID id,
        UUID authorId,
        String title,
        String slug,
        String excerpt,
        String coverImageUrl,
        String language,
        PostStatus status,
        int readingTimeMinutes,
        long viewCount,
        CategorySummaryDto category,
        List<String> tags,
        Instant publishedAt
) {
    public static PostSummaryDto from(Post post) {
        List<String> tags = post.getPostTags().stream()
                .map(pt -> pt.getTag().getName())
                .sorted()
                .toList();
        return new PostSummaryDto(
                post.getId(), post.getAuthorId(), post.getTitle(), post.getSlug(),
                post.getExcerpt(), post.getCoverImageUrl(), post.getLanguage(),
                post.getStatus(), post.getReadingTimeMinutes(), post.getViewCount(),
                CategorySummaryDto.from(post.getCategory()), tags, post.getPublishedAt()
        );
    }
}
