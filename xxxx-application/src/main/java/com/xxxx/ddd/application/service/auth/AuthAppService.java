package com.xxxx.ddd.application.service.auth;

import com.xxxx.ddd.application.model.auth.AuthUserResponse;
import com.xxxx.ddd.application.model.auth.IssuedAuthSession;
import com.xxxx.ddd.application.model.auth.LoginCommand;
import com.xxxx.ddd.application.model.auth.RegisterCommand;

public interface AuthAppService {

    IssuedAuthSession register(RegisterCommand command);

    IssuedAuthSession login(LoginCommand command);

    IssuedAuthSession refresh(String refreshToken);

    void logout(String refreshToken);

    AuthUserResponse me(Long userId);
}
