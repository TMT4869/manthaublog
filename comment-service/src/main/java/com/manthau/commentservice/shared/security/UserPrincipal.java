package com.manthau.commentservice.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public record UserPrincipal(UUID userId, String role) {

    public static UserPrincipal current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
            return p;
        }
        return null;
    }

    public static UUID currentId() {
        UserPrincipal p = current();
        return p != null ? p.userId() : null;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role);
    }
}
