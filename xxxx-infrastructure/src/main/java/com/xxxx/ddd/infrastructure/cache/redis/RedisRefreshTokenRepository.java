package com.xxxx.ddd.infrastructure.cache.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.ddd.domain.model.auth.RefreshTokenSession;
import com.xxxx.ddd.domain.respository.auth.RefreshTokenRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Repository
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private static final String KEY_PREFIX = "AUTH:REFRESH:";

    private final RedisTemplate<Object, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisRefreshTokenRepository(RedisTemplate<Object, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(RefreshTokenSession session, Duration ttl) {
        redisTemplate.opsForValue().set(key(session.getTokenHash()), session, ttl);
    }

    @Override
    public Optional<RefreshTokenSession> findByHash(String tokenHash) {
        Object value = redisTemplate.opsForValue().get(key(tokenHash));
        return toSession(value);
    }

    @Override
    public Optional<RefreshTokenSession> consumeByHash(String tokenHash) {
        Object value = redisTemplate.opsForValue().getAndDelete(key(tokenHash));
        return toSession(value);
    }

    @Override
    public void deleteByHash(String tokenHash) {
        redisTemplate.delete(key(tokenHash));
    }

    private Optional<RefreshTokenSession> toSession(Object value) {
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof RefreshTokenSession session) {
            return Optional.of(session);
        }
        if (value instanceof Map<?, ?>) {
            return Optional.of(objectMapper.convertValue(value, RefreshTokenSession.class));
        }
        return Optional.empty();
    }

    private static String key(String tokenHash) {
        return KEY_PREFIX + tokenHash;
    }
}
