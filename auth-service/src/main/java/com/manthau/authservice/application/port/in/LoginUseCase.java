package com.manthau.authservice.application.port.in;

import com.manthau.authservice.application.dto.AuthResult;
import com.manthau.authservice.application.dto.LoginCommand;

public interface LoginUseCase {
    AuthResult login(LoginCommand command);
}
