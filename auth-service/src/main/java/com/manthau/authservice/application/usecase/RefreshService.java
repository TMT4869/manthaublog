package com.manthau.authservice.application.usecase;

import com.manthau.authservice.application.dto.AuthResult;
import com.manthau.authservice.application.exception.ApplicationException;
import com.manthau.authservice.application.port.in.RefreshUseCase;
import com.manthau.authservice.application.port.out.AccessTokenPort;
import com.manthau.authservice.application.port.out.RefreshTokenPort;
import com.manthau.authservice.application.port.out.UserPort;
import com.manthau.authservice.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshService implements RefreshUseCase {

    private final RefreshTokenPort refreshTokenPort;
    private final UserPort userPort;
    private final AccessTokenPort accessTokenPort;

    @Override
    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        UUID userId = refreshTokenPort.validate(rawRefreshToken)
                .orElseThrow(() -> ApplicationException.unauthorized("Invalid or expired refresh token"));

        User user = userPort.findById(userId)
                .orElseThrow(() -> ApplicationException.notFound("User not found"));

        String accessToken = accessTokenPort.generate(user);
        String newRefreshToken = refreshTokenPort.issue(userId);

        return new AuthResult(accessToken, newRefreshToken, accessTokenPort.expiresInSeconds());
    }
}
