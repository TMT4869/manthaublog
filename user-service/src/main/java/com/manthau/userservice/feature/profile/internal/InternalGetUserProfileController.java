package com.manthau.userservice.feature.profile.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InternalGetUserProfileController {

    private final InternalGetUserProfileHandler handler;

    @GetMapping("/api/internal/users/{id}")
    public InternalUserProfileDto getUserProfile(@PathVariable UUID id) {
        return handler.handle(id);
    }
}
