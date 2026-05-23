package com.xxxx.ddd.application.model.auth;

public record RegisterCommand(String email, String password, String fullName) {
}
