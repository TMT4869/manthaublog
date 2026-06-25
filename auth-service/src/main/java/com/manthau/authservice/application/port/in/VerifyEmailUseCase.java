package com.manthau.authservice.application.port.in;

public interface VerifyEmailUseCase {
    void verify(String token);
}
