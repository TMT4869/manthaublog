package com.manthau.authservice.application.port.in;

import com.manthau.authservice.application.dto.AuthResult;

public interface RefreshUseCase {
    AuthResult refresh(String rawRefreshToken);
}
