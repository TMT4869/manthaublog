package com.manthau.postservice.shared.security;

public record JwtClaims(String userId, String email, String role) {}
