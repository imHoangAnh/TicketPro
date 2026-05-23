package com.xxxx.ddd.application.model.auth;

import java.util.List;

public record AuthenticatedPrincipal(Long userId, String email, List<String> roles) {
}
