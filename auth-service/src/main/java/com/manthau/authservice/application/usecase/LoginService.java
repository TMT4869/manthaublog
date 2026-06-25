package com.manthau.authservice.application.usecase;

import com.manthau.authservice.application.dto.AuthResult;
import com.manthau.authservice.application.dto.LoginCommand;
import com.manthau.authservice.application.exception.ApplicationException;
import com.manthau.authservice.application.port.in.LoginUseCase;
import com.manthau.authservice.application.port.out.AccessTokenPort;
import com.manthau.authservice.application.port.out.PasswordHashPort;
import com.manthau.authservice.application.port.out.RefreshTokenPort;
import com.manthau.authservice.application.port.out.UserPort;
import com.manthau.authservice.domain.enums.AuthProvider;
import com.manthau.authservice.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final UserPort userPort;
    private final AccessTokenPort accessTokenPort;
    private final RefreshTokenPort refreshTokenPort;
    private final PasswordHashPort passwordHashPort;

    @Override
    @Transactional
    public AuthResult login(LoginCommand cmd) {
        String identifier = normalizeIdentifier(cmd.identifier());
        User user = findByIdentifier(identifier)
                .orElseThrow(() -> ApplicationException.unauthorized("Invalid credentials"));

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw ApplicationException.badRequest("This account uses " + user.getProvider() + " login");
        }
        if (!passwordHashPort.matches(cmd.rawPassword(), user.getPasswordHash())) {
            throw ApplicationException.unauthorized("Invalid credentials");
        }
        if (!user.isVerified()) {
            throw ApplicationException.unauthorized("Email not verified");
        }

        String accessToken = accessTokenPort.generate(user);
        String refreshToken = refreshTokenPort.issue(user.getId());

        return new AuthResult(accessToken, refreshToken, accessTokenPort.expiresInSeconds());
    }

    private Optional<User> findByIdentifier(String identifier) {
        if (identifier.contains("@")) {
            return userPort.findByEmail(identifier);
        }
        return userPort.findByUsername(identifier);
    }

    private String normalizeIdentifier(String identifier) {
        return identifier.trim().toLowerCase(Locale.ROOT);
    }
}
