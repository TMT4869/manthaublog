package com.manthau.authservice.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private Jwt jwt = new Jwt();
    private OAuth2 oauth2 = new OAuth2();
    private String baseUrl = "http://localhost:8081";

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpiration = 900_000L;
        private long refreshTokenExpiration = 604_800_000L;
    }

    @Getter
    @Setter
    public static class OAuth2 {
        private String redirectUri = "http://localhost:3000/oauth2/callback";
    }
}
