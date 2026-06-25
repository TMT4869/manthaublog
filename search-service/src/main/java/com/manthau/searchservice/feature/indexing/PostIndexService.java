package com.manthau.searchservice.feature.indexing;

import com.manthau.searchservice.feature.document.PostDocument;
import com.manthau.searchservice.feature.document.PostDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostIndexService {

    private final PostDocumentRepository repository;
    private final PostIndexClient indexClient;
    private final AuthorProfileClient authorProfileClient;

    public void indexPost(UUID postId) {
        InternalPostDto dto = indexClient.fetchPost(postId);
        AuthorProfileDto author = fetchAuthorProfile(dto);
        PostDocument doc = PostDocument.from(dto, author);
        repository.save(doc);
        log.info("Indexed post {}", postId);
    }

    public void unindexPost(String postId) {
        if (repository.existsById(postId)) {
            repository.deleteById(postId);
            log.info("Unindexed post {}", postId);
        }
    }

    private AuthorProfileDto fetchAuthorProfile(InternalPostDto dto) {
        if (dto.authorId() == null || dto.authorId().isBlank()) {
            return null;
        }

        try {
            return authorProfileClient.fetchAuthor(dto.authorId());
        } catch (Exception ex) {
            log.warn("Unable to enrich author profile for postId={}, authorId={}", dto.id(), dto.authorId(), ex);
            return null;
        }
    }
}
