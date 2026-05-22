package com.manthau.userservice.shared.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class JwksKeyProvider {

    private final URI jwksUri;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final ConcurrentMap<String, PublicKey> keyCache = new ConcurrentHashMap<>();
    private final Object refreshLock = new Object();

    public JwksKeyProvider(@Value("${app.jwt.jwks-uri}") String jwksUri, ObjectMapper objectMapper) {
        this.jwksUri = URI.create(jwksUri);
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    public PublicKey keyFor(String token) {
        JwtHeader header = readHeader(token);
        PublicKey cached = keyCache.get(header.kid());
        if (cached != null) {
            return cached;
        }

        synchronized (refreshLock) {
            cached = keyCache.get(header.kid());
            if (cached != null) {
                return cached;
            }

            refreshKeys();
            PublicKey refreshed = keyCache.get(header.kid());
            if (refreshed == null) {
                throw new JwtException("No JWK found for kid: " + header.kid());
            }
            return refreshed;
        }
    }

    private JwtHeader readHeader(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) {
                throw new JwtException("JWT must have 3 parts");
            }

            byte[] decodedHeader = Base64.getUrlDecoder().decode(parts[0]);
            Map<?, ?> header = objectMapper.readValue(decodedHeader, Map.class);
            String kid = header.get("kid") instanceof String value ? value : null;
            String alg = header.get("alg") instanceof String value ? value : null;

            if (!StringUtils.hasText(kid)) {
                throw new JwtException("JWT header is missing kid");
            }
            if (!"RS256".equals(alg)) {
                throw new JwtException("Unsupported JWT alg: " + alg);
            }

            return new JwtHeader(kid);
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtException("Failed to read JWT header", e);
        }
    }

    private void refreshKeys() {
        try {
            HttpRequest request = HttpRequest.newBuilder(jwksUri)
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new JwtException("JWKS endpoint returned HTTP " + response.statusCode());
            }

            JwkSet jwkSet = objectMapper.readValue(response.body(), JwkSet.class);
            if (jwkSet.keys() == null) {
                return;
            }

            for (Jwk jwk : jwkSet.keys()) {
                if ("RSA".equals(jwk.kty()) && StringUtils.hasText(jwk.kid())
                        && StringUtils.hasText(jwk.n()) && StringUtils.hasText(jwk.e())) {
                    keyCache.put(jwk.kid(), toPublicKey(jwk));
                }
            }
        } catch (JwtException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JwtException("JWKS refresh interrupted", e);
        } catch (Exception e) {
            throw new JwtException("Failed to refresh JWKS", e);
        }
    }

    private PublicKey toPublicKey(Jwk jwk) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] modulusBytes = Base64.getUrlDecoder().decode(jwk.n());
        byte[] exponentBytes = Base64.getUrlDecoder().decode(jwk.e());
        BigInteger modulus = new BigInteger(1, modulusBytes);
        BigInteger exponent = new BigInteger(1, exponentBytes);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private record JwtHeader(String kid) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record JwkSet(List<Jwk> keys) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Jwk(String kty, String kid, String use, String alg, String n, String e) {
    }
}
