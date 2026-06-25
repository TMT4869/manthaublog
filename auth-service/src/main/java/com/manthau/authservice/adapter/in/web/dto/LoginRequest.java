package com.manthau.authservice.adapter.in.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        String identifier,
        String email,
        @NotBlank String password
) {
    public String loginIdentifier() {
        if (identifier != null && !identifier.isBlank()) {
            return identifier;
        }
        return email;
    }

    @AssertTrue(message = "Username or email is required")
    public boolean hasIdentifier() {
        return loginIdentifier() != null && !loginIdentifier().isBlank();
    }
}
