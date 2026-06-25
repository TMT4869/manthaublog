package com.manthau.authservice.application.port.in;

import com.manthau.authservice.application.dto.AuthResult;
import com.manthau.authservice.application.dto.RegisterCommand;

public interface RegisterUseCase {
    AuthResult register(RegisterCommand command);
}
