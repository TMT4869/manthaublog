package com.manthau.authservice.application.port.out;

import com.manthau.authservice.domain.enums.AuthProvider;
import com.manthau.authservice.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserPort {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
    Optional<User> findById(UUID id);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    User save(User user);
}
