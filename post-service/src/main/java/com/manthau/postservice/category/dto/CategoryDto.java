package com.manthau.postservice.category.dto;

import com.manthau.postservice.category.domain.Category;

import java.time.Instant;
import java.util.UUID;

public record CategoryDto(
        UUID id,
        String name,
        String slug,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    public static CategoryDto from(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
