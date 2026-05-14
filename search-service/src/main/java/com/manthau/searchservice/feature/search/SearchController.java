package com.manthau.searchservice.feature.search;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchHandler handler;

    @GetMapping("/api/search")
    public List<SearchResult> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String author
    ) {
        return handler.search(q, lang, tag, author);
    }

    @GetMapping("/api/search/suggestions")
    public List<SuggestionResult> suggestions(@RequestParam String q) {
        return handler.getSuggestions(q);
    }
}
