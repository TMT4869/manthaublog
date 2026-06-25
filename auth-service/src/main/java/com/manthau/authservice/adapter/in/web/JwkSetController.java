package com.manthau.authservice.adapter.in.web;

import com.manthau.authservice.adapter.out.security.JwtKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwkSetController {

    private final JwtKeyProvider jwtKeyProvider;

    @GetMapping("/.well-known/jwks.json")
    Map<String, Object> jwks() {
        return jwtKeyProvider.jwks();
    }
}
