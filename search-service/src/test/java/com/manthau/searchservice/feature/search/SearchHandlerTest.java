package com.manthau.searchservice.feature.search;

import com.manthau.searchservice.feature.document.PostDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchHandlerTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private SearchHits<PostDocument> searchHits;

    @InjectMocks
    private SearchHandler handler;

    @Test
    void categoryOnlySearchExecutesElasticsearchQuery() {
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(PostDocument.class)))
                .thenReturn(searchHits);
        when(searchHits.stream()).thenReturn(Stream.empty());

        handler.search(new SearchCriteria(null, null, null, "backend", null));

        verify(elasticsearchOperations).search(any(NativeQuery.class), eq(PostDocument.class));
    }

    @Test
    void blankSearchWithoutFiltersReturnsEmptyWithoutQuery() {
        handler.search(new SearchCriteria(null, null, null, null, null));

        verifyNoInteractions(elasticsearchOperations);
    }
}
