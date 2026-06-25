package com.manthau.userservice.feature.profile;

import com.manthau.userservice.shared.exception.AccessDeniedException;
import com.manthau.userservice.shared.security.UserPrincipal;
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

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getPublicProfile(@PathVariable String username) {
        UUID requesterId = UserPrincipal.currentId();
        return ResponseEntity.ok(userService.getPublicProfile(username, requesterId));
    }

    // =================== USER (SIGNED IN) ===================

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile(UserPrincipal.currentId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(UserPrincipal.currentId(), request));
    }

    // =================== ADMIN OR ACCOUNT OWNER ===================

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        UserPrincipal principal = UserPrincipal.current();

        boolean isOwner = principal.userId().equals(userId);
        boolean isAdmin = principal.isAdmin();

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to delete this account");
        }

        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // =================== ADMIN only ===================

    @PatchMapping("/{userId}/ban")
    public ResponseEntity<Void> banUser(@PathVariable UUID userId) {
        if (!UserPrincipal.current().isAdmin()) {
            throw new AccessDeniedException("Admin access required");
        }
        userService.banUser(userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/unban")
    public ResponseEntity<Void> unbanUser(@PathVariable UUID userId) {
        if (!UserPrincipal.current().isAdmin()) {
            throw new AccessDeniedException("Admin access required");
        }
        userService.unbanUser(userId);
        return ResponseEntity.ok().build();
    }
}
