package com.manthau.searchservice.feature.search;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.manthau.searchservice.feature.document.PostDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchHandler {

    private final ElasticsearchOperations elasticsearchOperations;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final int MAX_RESULTS = 10;
    private static final int MAX_SUGGESTIONS = 5;
    private static final Duration SUGGESTIONS_TTL = Duration.ofHours(1);

    public List<SearchResult> search(SearchCriteria criteria) {
        if (criteria.isEmpty()) {
            return List.of();
        }

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(buildSearchQuery(criteria))
                .withMaxResults(MAX_RESULTS)
                .build();

        SearchHits<PostDocument> hits = elasticsearchOperations.search(nativeQuery, PostDocument.class);
        return hits.stream()
                .map(SearchHit::getContent)
                .map(SearchResult::from)
                .toList();
    }

    private Query buildSearchQuery(SearchCriteria criteria) {
        return Query.of(qb -> qb.bool(b -> {
            addKeywordQuery(b, criteria);
            addAuthorQuery(b, criteria);
            addPublishedFilter(b);
            addTermFilter(b, "language", criteria.language());
            addTermFilter(b, "tags", criteria.tagSlug());
            addTermFilter(b, "category_slug", criteria.categorySlug());
            return b;
        }));
    }

    private void addKeywordQuery(Builder builder, SearchCriteria criteria) {
        if (criteria.query() == null) {
            return;
        }

        builder.must(m -> m.multiMatch(mm -> mm
                .query(criteria.query())
                .fields("title^3", "excerpt^2", "author_name^2", "author_name_tag", "content")
        ));
    }

    private void addAuthorQuery(Builder builder, SearchCriteria criteria) {
        if (criteria.authorQuery() == null) {
            return;
        }

        builder.must(m -> m.bool(authorBool -> authorBool
                .should(s -> s.matchPhrasePrefix(mp -> mp.field("author_name").query(criteria.authorQuery())))
                .should(s -> s.prefix(p -> p.field("author_name_tag").value(criteria.authorNameTagQuery())))
                .minimumShouldMatch("1")
        ));
    }

    private void addPublishedFilter(Builder builder) {
        addTermFilter(builder, "status", "published");
    }

    private void addTermFilter(Builder builder, String field, String value) {
        if (value == null) {
            return;
        }

        builder.filter(f -> f.term(t -> t.field(field).value(value)));
    }

    @SuppressWarnings("unchecked")
    public List<SuggestionResult> getSuggestions(String prefix) {
        if (prefix == null || prefix.isBlank()) return List.of();

        String cacheKey = "search:suggestions:" + prefix.toLowerCase();
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List<?> list && !list.isEmpty()) {
            try {
                return (List<SuggestionResult>) list;
            } catch (ClassCastException e) {
                log.warn("Stale suggestion cache entry for key={}", cacheKey, e);
            }
        }

        Query esQuery = Query.of(q -> q.bool(b -> b
                .must(m -> m.matchPhrasePrefix(mp -> mp.field("title").query(prefix)))
                .filter(f -> f.term(t -> t.field("status").value("published")))
        ));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(esQuery)
                .withMaxResults(MAX_SUGGESTIONS)
                .build();

        SearchHits<PostDocument> hits = elasticsearchOperations.search(nativeQuery, PostDocument.class);
        List<SuggestionResult> suggestions = hits.stream()
                .map(SearchHit::getContent)
                .map(doc -> new SuggestionResult(doc.getTitle(), doc.getSlug()))
                .toList();

        if (!suggestions.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, suggestions, SUGGESTIONS_TTL);
        }
        return suggestions;
    }

}
