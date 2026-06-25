package com.manthau.userservice.feature.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByDisplayNameAndNameTag(String displayName, String nameTag);

    boolean existsByDisplayNameAndNameTagAndIdNot(String displayName, String nameTag, UUID id);

    @Modifying
    @Query("UPDATE UserProfile u SET u.followersCount = u.followersCount + 1 WHERE u.id = :userId")
    void incrementFollowersCount(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE UserProfile u SET u.followersCount = u.followersCount - 1 WHERE u.id = :userId AND u.followersCount > 0")
    void decrementFollowersCount(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE UserProfile u SET u.followingCount = u.followingCount + 1 WHERE u.id = :userId")
    void incrementFollowingCount(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE UserProfile u SET u.followingCount = u.followingCount - 1 WHERE u.id = :userId AND u.followingCount > 0")
    void decrementFollowingCount(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE UserProfile u SET u.postsCount = u.postsCount + 1 WHERE u.id = :userId")
    void incrementPostsCount(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE UserProfile u SET u.postsCount = u.postsCount - 1 WHERE u.id = :userId AND u.postsCount > 0")
    void decrementPostsCount(@Param("userId") UUID userId);
}
