package com.xxxx.ddd.application.model.auth;

public record AuthTokenResponse(String accessToken, long expiresInSeconds, AuthUserResponse user) {
}
