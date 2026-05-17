package com.manthau.postservice.post.get;

import com.manthau.postservice.category.dto.CategorySummaryDto;
import com.manthau.postservice.post.domain.Post;
import com.manthau.postservice.post.domain.PostStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PostDetailDto(
        UUID id,
        UUID authorId,
        String title,
        String slug,
        String content,
        String contentHtml,
        String excerpt,
        String coverImageUrl,
        String language,
        List<Map<String, Object>> toc,
        PostStatus status,
        int readingTimeMinutes,
        long viewCount,
        CategorySummaryDto category,
        List<String> tags,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static PostDetailDto from(Post post) {
        List<String> tags = post.getPostTags().stream()
                .map(pt -> pt.getTag().getName())
                .sorted()
                .toList();
        return new PostDetailDto(
                post.getId(), post.getAuthorId(), post.getTitle(), post.getSlug(),
                post.getContent(), post.getContentHtml(), post.getExcerpt(),
                post.getCoverImageUrl(), post.getLanguage(), post.getToc(),
                post.getStatus(), post.getReadingTimeMinutes(), post.getViewCount(),
                CategorySummaryDto.from(post.getCategory()), tags,
                post.getPublishedAt(), post.getCreatedAt(), post.getUpdatedAt()
        );
    }
}
