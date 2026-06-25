package com.manthau.postservice.post.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePostRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String content,
        @Size(max = 500) String excerpt,
        @Size(max = 500) String coverImageUrl,
        @NotBlank String categorySlug,
        List<String> tagSlugs
) {}
