package com.manthau.searchservice.feature.search;

import java.util.Locale;

record SearchCriteria(
        String query,
        String language,
        String tagSlug,
        String categorySlug,
        String authorQuery
) {

    SearchCriteria {
        query = normalized(query);
        language = normalized(language);
        tagSlug = normalized(tagSlug);
        categorySlug = normalized(categorySlug);
        authorQuery = normalized(authorQuery);
    }

    boolean isEmpty() {
        return query == null && tagSlug == null && categorySlug == null && authorQuery == null;
    }

    String authorNameTagQuery() {
        return authorQuery != null ? authorQuery.toUpperCase(Locale.ROOT) : null;
    }

    private static String normalized(String value) {
        return value != null && !value.isBlank() ? value.trim() : null;
    }
}
