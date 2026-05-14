package com.manthau.authservice.adapter.out.persistence.jpa;

import com.manthau.authservice.adapter.out.persistence.entity.UserAuthEntity;
import com.manthau.authservice.domain.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserAuthEntity, UUID> {
    Optional<UserAuthEntity> findByEmail(String email);
    Optional<UserAuthEntity> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<UserAuthEntity> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
