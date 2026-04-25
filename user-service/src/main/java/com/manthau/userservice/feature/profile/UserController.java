package com.manthau.userservice.feature.profile;

import com.manthau.userservice.shared.exception.AccessDeniedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // =================== PUBLIC ===================

    // Ai cũng xem được — kể cả anonymous (không cần header)
    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getPublicProfile(
            @PathVariable String username,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        // Nếu đã login thì trả về kèm isFollowing, nếu anonymous thì isFollowing = null
        return ResponseEntity.ok(userService.getPublicProfile(username, requesterId));
    }

    // =================== USER (đã login) ===================

    // Chỉ user đã login mới gọi được — Gateway đảm bảo X-User-Id luôn có
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(userService.getMyProfile(userId));
    }

    // Chỉ chính chủ mới update được profile của mình
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    // =================== ADMIN hoặc CHÍNH CHỦ ===================

    // Xóa tài khoản — chỉ chính chủ hoặc ADMIN
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId,
            @RequestHeader("X-User-Id") UUID requesterId,
            @RequestHeader("X-User-Role") String requesterRole) {

        boolean isOwner = requesterId.equals(userId);
        boolean isAdmin = "ROLE_ADMIN".equals(requesterRole);

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to delete this account");
        }

        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // =================== ADMIN only ===================

    // Khóa tài khoản — chỉ ADMIN
    @PatchMapping("/{userId}/ban")
    public ResponseEntity<Void> banUser(
            @PathVariable UUID userId,
            @RequestHeader("X-User-Role") String requesterRole) {

        if (!"ROLE_ADMIN".equals(requesterRole)) {
            throw new AccessDeniedException("Admin access required");
        }

        userService.banUser(userId);
        return ResponseEntity.ok().build();
    }

    // Mở khóa tài khoản — chỉ ADMIN
    @PatchMapping("/{userId}/unban")
    public ResponseEntity<Void> unbanUser(
            @PathVariable UUID userId,
            @RequestHeader("X-User-Role") String requesterRole) {

        if (!"ROLE_ADMIN".equals(requesterRole)) {
            throw new AccessDeniedException("Admin access required");
        }

        userService.unbanUser(userId);
        return ResponseEntity.ok().build();
    }
}