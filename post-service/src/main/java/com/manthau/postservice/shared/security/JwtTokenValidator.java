package com.manthau.postservice.shared.security;

public interface JwtTokenValidator {
    JwtClaims validate(String token);
}
