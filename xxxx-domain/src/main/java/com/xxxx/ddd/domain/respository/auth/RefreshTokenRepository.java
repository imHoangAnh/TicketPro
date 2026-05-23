package com.xxxx.ddd.domain.respository.auth;

import com.xxxx.ddd.domain.model.auth.RefreshTokenSession;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenRepository {

    void save(RefreshTokenSession session, Duration ttl);

    Optional<RefreshTokenSession> findByHash(String tokenHash);

    Optional<RefreshTokenSession> consumeByHash(String tokenHash);

    void deleteByHash(String tokenHash);
}
