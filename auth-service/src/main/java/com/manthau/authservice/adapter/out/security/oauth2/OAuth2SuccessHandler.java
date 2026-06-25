package com.manthau.authservice.adapter.out.security.oauth2;

import com.manthau.authservice.application.port.out.AccessTokenPort;
import com.manthau.authservice.application.port.out.RefreshTokenPort;
import com.manthau.authservice.infrastructure.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AccessTokenPort accessTokenPort;
    private final RefreshTokenPort refreshTokenPort;
    private final AppProperties appProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();
        var user = principal.getUser();

        String accessToken = accessTokenPort.generate(user);
        String refreshToken = refreshTokenPort.issue(user.getId());

        String redirectUri = UriComponentsBuilder
                .fromUriString(appProperties.getOauth2().getRedirectUri())
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}
