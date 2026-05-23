package com.xxxx.ddd.domain.model.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RefreshTokenSession {

    private String tokenHash;
    private Long userId;
    private String email;
    private List<String> roles;
    private Instant issuedAt;
    private Instant expiresAt;

    public boolean isExpired(Instant now) {
        return expiresAt == null || !expiresAt.isAfter(now);
    }
}
