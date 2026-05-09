package com.manthau.searchservice.feature.search;

import java.io.Serializable;

public record SuggestionResult(String title, String slug) implements Serializable {}
