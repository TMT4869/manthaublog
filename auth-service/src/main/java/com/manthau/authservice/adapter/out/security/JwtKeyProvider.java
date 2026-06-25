package com.manthau.authservice.adapter.out.security;

import com.manthau.authservice.infrastructure.config.AppProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtKeyProvider {

    private final AppProperties appProperties;

    private KeyPair keyPair;
    private String keyId;

    @PostConstruct
    void init() {
        keyPair = loadConfiguredKeyPair();
        keyId = configuredOrThumbprintKeyId((RSAPublicKey) keyPair.getPublic());
    }

    public RSAPrivateKey privateKey() {
        return (RSAPrivateKey) keyPair.getPrivate();
    }

    public RSAPublicKey publicKey() {
        return (RSAPublicKey) keyPair.getPublic();
    }

    public String keyId() {
        return keyId;
    }

    public Map<String, Object> jwks() {
        RSAPublicKey publicKey = publicKey();
        return Map.of("keys", List.of(Map.of(
                "kty", "RSA",
                "use", "sig",
                "alg", "RS256",
                "kid", keyId(),
                "n", base64UrlUnsigned(publicKey.getModulus()),
                "e", base64UrlUnsigned(publicKey.getPublicExponent())
        )));
    }

    private KeyPair loadConfiguredKeyPair() {
        String configuredPrivateKey = appProperties.getJwt().getPrivateKey();
        if (!StringUtils.hasText(configuredPrivateKey)) {
            return generateKeyPair();
        }

        try {
            String normalized = configuredPrivateKey
                    .replace("\\n", "\n")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(normalized);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
            if (!(privateKey instanceof RSAPrivateCrtKey rsaPrivateKey)) {
                throw new IllegalStateException("JWT private key must be an RSA PKCS#8 private key");
            }

            var publicKeySpec = new RSAPublicKeySpec(
                    rsaPrivateKey.getModulus(),
                    rsaPrivateKey.getPublicExponent()
            );
            return new KeyPair(keyFactory.generatePublic(publicKeySpec), privateKey);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load JWT private key", e);
        }
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA key generation is not available", e);
        }
    }

    private String configuredOrThumbprintKeyId(RSAPublicKey publicKey) {
        String configuredKeyId = appProperties.getJwt().getKeyId();
        if (StringUtils.hasText(configuredKeyId)) {
            return configuredKeyId;
        }

        String n = base64UrlUnsigned(publicKey.getModulus());
        String e = base64UrlUnsigned(publicKey.getPublicExponent());
        String jwkThumbprintPayload = "{\"e\":\"" + e + "\",\"kty\":\"RSA\",\"n\":\"" + n + "\"}";
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                    .digest(jwkThumbprintPayload.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    private String base64UrlUnsigned(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            bytes = trimmed;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
