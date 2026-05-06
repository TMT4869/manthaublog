package com.manthau.postservice.admin.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "post_review_history")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReviewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewAction action;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
}
