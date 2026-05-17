package com.manthau.postservice.category.dto;

import com.manthau.postservice.category.domain.Category;

public record CategorySummaryDto(String name, String slug) {

    public static CategorySummaryDto from(Category category) {
        return new CategorySummaryDto(category.getName(), category.getSlug());
    }
}
