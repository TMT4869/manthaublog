package com.manthau.postservice.admin.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostReviewHistoryRepository extends JpaRepository<PostReviewHistory, UUID> {
    List<PostReviewHistory> findByPostIdOrderByCreatedAtDesc(UUID postId);
}
