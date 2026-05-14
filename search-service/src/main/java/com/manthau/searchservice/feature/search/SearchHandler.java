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
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchHandler {

    private final ElasticsearchOperations elasticsearchOperations;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final int MAX_RESULTS = 10;
    private static final int MAX_SUGGESTIONS = 5;
    private static final Duration SUGGESTIONS_TTL = Duration.ofHours(1);

    public List<SearchResult> search(String q, String lang, String tag, String author) {
        if (!hasText(q) && !hasText(tag) && !hasText(author)) {
            return List.of();
        }

        final String query = normalized(q);
        final String language = normalized(lang);
        final String tagSlug = normalized(tag);
        final String authorQuery = normalized(author);
        final String authorNameTagQuery = authorQuery != null ? authorQuery.toUpperCase(Locale.ROOT) : null;

        Query esQuery = Query.of(qb -> qb.bool(b -> {
            if (query != null) {
                b.must(m -> m.multiMatch(mm -> mm
                        .query(query)
                        .fields("title^3", "excerpt^2", "author_name^2", "author_name_tag", "content")
                ));
            }

            if (authorQuery != null) {
                b.must(m -> m.bool(authorBool -> authorBool
                        .should(s -> s.matchPhrasePrefix(mp -> mp.field("author_name").query(authorQuery)))
                        .should(s -> s.prefix(p -> p.field("author_name_tag").value(authorNameTagQuery)))
                        .minimumShouldMatch("1")
                ));
            }

            b.filter(f -> f.term(t -> t.field("status").value("published")));
            if (language != null) {
                b.filter(f -> f.term(t -> t.field("language").value(language)));
            }
            if (tagSlug != null) {
                b.filter(f -> f.term(t -> t.field("tags").value(tagSlug)));
            }
            return b;
        }));

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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalized(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
