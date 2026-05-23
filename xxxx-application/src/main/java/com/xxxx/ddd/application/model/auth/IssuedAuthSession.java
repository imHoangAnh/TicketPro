package com.xxxx.ddd.application.model.auth;

public record IssuedAuthSession(AuthTokenResponse response, String refreshToken) {
}
