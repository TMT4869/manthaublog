package com.manthau.authservice.application.usecase;

import com.manthau.authservice.application.exception.ApplicationException;
import com.manthau.authservice.application.port.in.VerifyEmailUseCase;
import com.manthau.authservice.application.port.out.EmailVerificationPort;
import com.manthau.authservice.application.port.out.UserPort;
import com.manthau.authservice.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class VerifyEmailService implements VerifyEmailUseCase {

    private final EmailVerificationPort emailVerificationPort;
    private final UserPort userPort;

    @Override
    @Transactional
    public void verify(String token) {
        var userId = emailVerificationPort.consumeToken(token)
                .orElseThrow(() -> ApplicationException.badRequest("Invalid or expired verification token"));

        User user = userPort.findById(userId)
                .orElseThrow(() -> ApplicationException.notFound("User not found"));

        user.setVerified(true);
        user.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        userPort.save(user);
    }
}
