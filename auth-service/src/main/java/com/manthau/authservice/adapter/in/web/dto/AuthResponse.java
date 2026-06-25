package com.manthau.authservice.adapter.in.web.dto;

import com.manthau.authservice.application.dto.AuthResult;

public record AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {

    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(result.accessToken(), result.refreshToken(), "Bearer", result.expiresInSeconds());
    }
}
