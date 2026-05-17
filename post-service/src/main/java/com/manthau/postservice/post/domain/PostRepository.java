package com.manthau.postservice.post.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = :status AND (:language IS NULL OR p.language = :language) AND (:authorId IS NULL OR p.authorId = :authorId) AND (:categorySlug IS NULL OR p.category.slug = :categorySlug)")
    Page<Post> findPublished(PostStatus status, String language, UUID authorId, String categorySlug, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.postTags pt WHERE pt.tag.slug = :tagSlug AND p.status = 'published' ORDER BY p.publishedAt DESC")
    Page<Post> findByTagSlug(String tagSlug, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.category.slug = :categorySlug AND p.status = :status")
    Page<Post> findByCategorySlug(String categorySlug, PostStatus status, Pageable pageable);

    boolean existsByCategorySlug(String categorySlug);

    @Query("SELECT p FROM Post p WHERE p.authorId IN :authorIds AND p.status = 'published' ORDER BY p.publishedAt DESC")
    Page<Post> findFeedForAuthors(java.util.List<UUID> authorIds, Pageable pageable);
}
