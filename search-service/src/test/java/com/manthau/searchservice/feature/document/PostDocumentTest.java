package com.manthau.searchservice.feature.document;

import com.manthau.searchservice.feature.indexing.InternalPostDto;
import com.manthau.searchservice.feature.search.SearchResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PostDocumentTest {

    @Test
    void mapsCategorySlugFromInternalPostToDocumentAndSearchResult() {
        InternalPostDto dto = new InternalPostDto(
                "post-1",
                "author-1",
                null,
                null,
                "Title",
                "title",
                "Excerpt",
                "Content",
                "en",
                "backend",
                List.of("java"),
                Instant.EPOCH
        );

        PostDocument document = PostDocument.from(dto);
        SearchResult result = SearchResult.from(document);

        assertThat(document.getCategorySlug()).isEqualTo("backend");
        assertThat(result.categorySlug()).isEqualTo("backend");
    }
}
