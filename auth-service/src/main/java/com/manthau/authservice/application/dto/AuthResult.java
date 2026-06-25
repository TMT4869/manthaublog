package com.manthau.authservice.application.dto;

public record AuthResult(String accessToken, String refreshToken, long expiresInSeconds) {}
