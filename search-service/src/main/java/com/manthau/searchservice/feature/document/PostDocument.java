package com.manthau.searchservice.feature.document;

import com.manthau.searchservice.feature.indexing.AuthorProfileDto;
import com.manthau.searchservice.feature.indexing.InternalPostDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.List;

@Document(indexName = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String excerpt;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String content;

    @Field(type = FieldType.Keyword, name = "author_id")
    private String authorId;

    @Field(type = FieldType.Text, name = "author_name")
    private String authorName;

    @Field(type = FieldType.Keyword, name = "author_name_tag")
    private String authorNameTag;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Keyword)
    private String language;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Date, name = "published_at")
    private Instant publishedAt;

    @Field(type = FieldType.Keyword)
    private String slug;

    public static PostDocument from(InternalPostDto dto) {
        return from(dto, null);
    }

    public static PostDocument from(InternalPostDto dto, AuthorProfileDto author) {
        return PostDocument.builder()
                .id(dto.id())
                .title(dto.title())
                .excerpt(dto.excerpt())
                .content(dto.content())
                .authorId(dto.authorId())
                .authorName(firstNonBlank(author != null ? author.displayName() : null, dto.authorName()))
                .authorNameTag(firstNonBlank(author != null ? author.nameTag() : null, dto.authorNameTag()))
                .tags(dto.tags())
                .language(dto.language())
                .status("published")
                .publishedAt(dto.publishedAt())
                .slug(dto.slug())
                .build();
    }

    private static String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        return fallback;
    }

    public String getAuthorFullDisplayName() {
        if (authorNameTag == null || authorNameTag.isBlank()) {
            return authorName;
        }
        return authorName + "#" + authorNameTag;
    }
}
