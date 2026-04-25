package com.manthau.userservice.feature.follow;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Follow.FollowId> {

    boolean existsByIdFollowerIdAndIdFollowingId(UUID followerId, UUID followingId);

    void deleteByIdFollowerIdAndIdFollowingId(UUID followerId, UUID followingId);

    // Lấy thông tin followers của 1 user (ai đang follow user này)
    @Query("""
        SELECT new FollowUserResponse(
            u.id, u.username, u.displayName, u.avatarUrl)
        FROM Follow f
        JOIN UserProfile u ON u.id = f.id.followerId
        WHERE f.id.followingId = :userId
        """)
    Page<FollowUserResponse> findFollowersByUserId(@Param("userId") UUID userId, Pageable pageable);

    // Lấy thông tin following của 1 user (user này đang follow ai)
    @Query("""
        SELECT new FollowUserResponse(
            u.id, u.username, u.displayName, u.avatarUrl)
        FROM Follow f
        JOIN UserProfile u ON u.id = f.id.followingId
        WHERE f.id.followerId = :userId
        """)
    Page<FollowUserResponse> findFollowingByUserId(@Param("userId") UUID userId, Pageable pageable);

}