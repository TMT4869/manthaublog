package com.manthau.authservice.application.port.out;

import com.manthau.authservice.domain.model.User;

public interface AccessTokenPort {
    String generate(User user);
    long expiresInSeconds();
}
