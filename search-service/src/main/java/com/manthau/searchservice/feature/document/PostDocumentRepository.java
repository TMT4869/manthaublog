package com.manthau.searchservice.feature.document;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostDocumentRepository extends ElasticsearchRepository<PostDocument, String> {
}
