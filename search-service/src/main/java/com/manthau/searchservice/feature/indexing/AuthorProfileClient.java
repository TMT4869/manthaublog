package com.manthau.searchservice.feature.indexing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthorProfileClient {

    private final RestClient restClient;

    public AuthorProfileClient(@Value("${app.user-service.url}") String userServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    public AuthorProfileDto fetchAuthor(String authorId) {
        return restClient.get()
                .uri("/api/internal/users/{id}", authorId)
                .retrieve()
                .body(AuthorProfileDto.class);
    }
}
