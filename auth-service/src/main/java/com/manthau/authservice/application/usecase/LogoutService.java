package com.manthau.authservice.application.usecase;

import com.manthau.authservice.application.port.in.LogoutUseCase;
import com.manthau.authservice.application.port.out.RefreshTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenPort refreshTokenPort;

    @Override
    @Transactional
    public void logout(UUID userId) {
        refreshTokenPort.revokeAll(userId);
    }
}
