package com.xxxx.ddd.application.model.auth;

import java.util.List;

public record AuthUserResponse(Long userId, String email, String fullName, List<String> roles) {
}
