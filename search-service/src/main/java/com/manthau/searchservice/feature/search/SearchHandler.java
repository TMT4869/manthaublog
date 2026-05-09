package com.manthau.searchservice.feature.search;

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

    public List<SearchResult> search(String q, String lang, String tag) {
        Query esQuery;

        if (tag != null && !tag.isBlank()) {
            esQuery = Query.of(qb -> qb.bool(b -> b
                    .must(m -> m.term(t -> t.field("tags").value(tag)))
                    .filter(f -> f.term(t -> t.field("status").value("published")))
            ));
        } else if (q != null && !q.isBlank()) {
            final String langFinal = (lang != null && !lang.isBlank()) ? lang : null;
            esQuery = Query.of(qb -> qb.bool(b -> {
                b.must(m -> m.multiMatch(mm -> mm
                        .query(q)
                        .fields("title^3", "excerpt^2", "content")
                ));
                b.filter(f -> f.term(t -> t.field("status").value("published")));
                if (langFinal != null) {
                    b.filter(f -> f.term(t -> t.field("language").value(langFinal)));
                }
                return b;
            }));
        } else {
            return List.of();
        }

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(esQuery)
                .withMaxResults(MAX_RESULTS)
                .build();

        SearchHits<PostDocument> hits = elasticsearchOperations.search(nativeQuery, PostDocument.class);
        return hits.stream()
                .map(SearchHit::getContent)
                .map(SearchResult::from)
                .toList();
    }

    @SuppressWarnings("unchecked")
    public List<SuggestionResult> getSuggestions(String prefix) {
        if (prefix == null || prefix.isBlank()) return List.of();

        String cacheKey = "search:suggestions:" + prefix.toLowerCase();
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List<?> list && !list.isEmpty()) {
            try {
                return (List<SuggestionResult>) list;
            } catch (ClassCastException ignored) {
                log.warn("Stale suggestion cache entry for key={}", cacheKey);
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
            redisTemplate.opsForValue().set(cacheKey, (Object) suggestions, SUGGESTIONS_TTL);
        }
        return suggestions;
    }
}
