package com.manthau.postservice.tag.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Tag> findBySlugIn(List<String> slugs);

    List<Tag> findTop20ByOrderByPostsCountDesc();
}
