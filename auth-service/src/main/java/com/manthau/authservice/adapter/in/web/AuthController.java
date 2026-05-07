package com.manthau.authservice.adapter.in.web;

import com.manthau.authservice.adapter.in.web.dto.*;
import com.manthau.authservice.application.dto.RegisterCommand;
import com.manthau.authservice.application.dto.LoginCommand;
import com.manthau.authservice.application.port.in.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshUseCase refreshUseCase;
    private final LogoutUseCase logoutUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;

    @PostMapping("/register")
    ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        var result = registerUseCase.register(
                new RegisterCommand(req.email(), req.password(), req.displayName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(result));
    }

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        var result = loginUseCase.login(new LoginCommand(req.email(), req.password()));
        return ResponseEntity.ok(AuthResponse.from(result));
    }

    @PostMapping("/refresh")
    ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        var result = refreshUseCase.refresh(req.refreshToken());
        return ResponseEntity.ok(AuthResponse.from(result));
    }

    @PostMapping("/logout")
    ResponseEntity<MessageResponse> logout(@AuthenticationPrincipal String userId) {
        logoutUseCase.logout(UUID.fromString(userId));
        return ResponseEntity.ok(MessageResponse.of("Logged out successfully"));
    }

    @GetMapping("/verify-email")
    ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        verifyEmailUseCase.verify(token);
        return ResponseEntity.ok(MessageResponse.of("Email verified successfully"));
    }
}
