package com.manthau.authservice.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationPort {
    /** Generates a one-time token, stores userId in Redis, returns the token. */
    String createToken(UUID userId);

    /** Consumes and deletes the token; returns the userId it was bound to. */
    Optional<UUID> consumeToken(String token);

    /** Sends the verification email asynchronously. */
    void sendVerificationEmail(String toEmail, String verificationUrl);
}
