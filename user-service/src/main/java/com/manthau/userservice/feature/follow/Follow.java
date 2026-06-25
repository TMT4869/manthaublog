package com.manthau.userservice.feature.follow;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "follows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Follow {

    @EmbeddedId
    private FollowId id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FollowId implements Serializable {
        @Column(name = "follower_id")
        private UUID followerId;

        @Column(name = "following_id")
        private UUID followingId;
    }
}