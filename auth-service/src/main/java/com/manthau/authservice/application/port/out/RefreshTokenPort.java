package com.manthau.authservice.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenPort {
    /** Generates a raw token, persists its hash in DB + Redis session, returns raw token. */
    String issue(UUID userId);

    /** Validates raw token; returns the owning userId if valid, empty otherwise. */
    Optional<UUID> validate(String rawToken);

    /** Revokes all active tokens for the user in DB and deletes Redis session. */
    void revokeAll(UUID userId);
}
