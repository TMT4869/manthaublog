package com.manthau.authservice.adapter.out.persistence;

import com.manthau.authservice.adapter.out.persistence.entity.UserAuthEntity;
import com.manthau.authservice.adapter.out.persistence.jpa.UserJpaRepository;
import com.manthau.authservice.application.port.out.UserPort;
import com.manthau.authservice.domain.enums.AuthProvider;
import com.manthau.authservice.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPort {

    private final UserJpaRepository jpaRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId) {
        return jpaRepository.findByProviderAndProviderId(provider, providerId).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public User save(User user) {
        UserAuthEntity entity = toEntity(user);
        return toDomain(jpaRepository.save(entity));
    }

    private User toDomain(UserAuthEntity e) {
        return User.builder()
                .id(e.getId())
                .username(e.getUsername())
                .email(e.getEmail())
                .passwordHash(e.getPasswordHash())
                .provider(e.getProvider())
                .providerId(e.getProviderId())
                .verified(e.isVerified())
                .role(e.getRole())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private UserAuthEntity toEntity(User u) {
        return UserAuthEntity.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .passwordHash(u.getPasswordHash())
                .provider(u.getProvider())
                .providerId(u.getProviderId())
                .verified(u.isVerified())
                .role(u.getRole())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}
