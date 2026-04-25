package com.manthau.userservice.feature.follow;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{username}/follow")
    public ResponseEntity<Void> follow(
            @RequestHeader("X-User-Id") UUID followerId,
            @PathVariable String username) {
        followService.follow(followerId, username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/follow")
    public ResponseEntity<Void> unfollow(
            @RequestHeader("X-User-Id") UUID followerId,
            @PathVariable String username) {
        followService.unfollow(followerId, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{username}/followers")
    public ResponseEntity<PagedResponse<FollowUserResponse>> getFollowers(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(followService.getFollowers(username, page, size));
    }

    @GetMapping("/{username}/following")
    public ResponseEntity<PagedResponse<FollowUserResponse>> getFollowing(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(followService.getFollowing(username, page, size));
    }
}