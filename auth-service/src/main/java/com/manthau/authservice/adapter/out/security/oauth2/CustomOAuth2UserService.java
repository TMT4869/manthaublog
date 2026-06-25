package com.manthau.authservice.adapter.out.security.oauth2;

import com.manthau.authservice.application.port.out.UserPort;
import com.manthau.authservice.domain.enums.AuthProvider;
import com.manthau.authservice.domain.enums.UserRole;
import com.manthau.authservice.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserPort userPort;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        if (oAuth2User == null) {
            throw new OAuth2AuthenticationException("Failed to load user from OAuth2 provider");
        }

        String registrationId = request.getClientRegistration().getRegistrationId();

        AuthProvider provider = switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "github" -> AuthProvider.GITHUB;
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        };

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String providerId = extractProviderId(provider, attributes);
        String email = (String) attributes.get("email");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not available from OAuth2 provider");
        }

        User user = userPort.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> userPort.findByEmail(email)
                        .orElseGet(() -> createUser(email, provider, providerId)));

        return new OAuth2UserPrincipal(user, attributes);
    }

    private User createUser(String email, AuthProvider provider, String providerId) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return userPort.save(User.builder()
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .verified(true)
                .role(UserRole.USER)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private String extractProviderId(AuthProvider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> (String) attributes.get("sub");
            case GITHUB -> String.valueOf(attributes.get("id"));
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }
}
