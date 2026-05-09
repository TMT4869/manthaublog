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

    public void indexPost(UUID postId) {
        InternalPostDto dto = indexClient.fetchPost(postId);
        PostDocument doc = PostDocument.from(dto);
        repository.save(doc);
        log.info("Indexed post {}", postId);
    }

    public void unindexPost(String postId) {
        if (repository.existsById(postId)) {
            repository.deleteById(postId);
            log.info("Unindexed post {}", postId);
        }
    }
}
