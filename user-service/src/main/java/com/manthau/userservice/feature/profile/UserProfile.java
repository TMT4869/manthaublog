package com.manthau.userservice.feature.profile;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_profiles",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_profiles_display_name_tag",
                columnNames = {"display_name", "name_tag"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id; // Cùng UUID với Auth Service, không auto-generate

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "name_tag", length = 6)
    private String nameTag;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(length = 255)
    private String website;

    @Column(length = 100)
    private String location;

    @Column(name = "followers_count", nullable = false)
    @Builder.Default
    private int followersCount = 0;

    @Column(name = "following_count", nullable = false)
    @Builder.Default
    private int followingCount = 0;

    @Column(name = "posts_count", nullable = false)
    @Builder.Default
    private int postsCount = 0;

    // ACTIVE: bình thường | BANNED: bị khóa bởi admin | DELETED: tự xóa tài khoản
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getFullDisplayName() {
        if (nameTag == null || nameTag.isBlank()) {
            return displayName;
        }
        return displayName + "#" + nameTag;
    }
}
