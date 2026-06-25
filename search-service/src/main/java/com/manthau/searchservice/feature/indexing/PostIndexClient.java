package com.manthau.searchservice.feature.indexing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class PostIndexClient {

    private final RestClient restClient;

    public PostIndexClient(@Value("${app.post-service.url}") String postServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(postServiceUrl)
                .build();
    }

    public InternalPostDto fetchPost(UUID postId) {
        return restClient.get()
                .uri("/api/internal/posts/{id}", postId)
                .retrieve()
                .body(InternalPostDto.class);
    }
}
