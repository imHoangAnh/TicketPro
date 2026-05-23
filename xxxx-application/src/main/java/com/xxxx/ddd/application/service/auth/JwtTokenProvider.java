package com.xxxx.ddd.application.service.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.ddd.application.model.auth.AuthenticatedPrincipal;
import com.xxxx.ddd.domain.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class JwtTokenProvider {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String ISSUER = "xxxx.com";
    private static final String AUDIENCE = "ticket-booking-api";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final byte[] secret;
    private final long accessTokenTtlSeconds;

    @Autowired
    public JwtTokenProvider(
            ObjectMapper objectMapper,
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.access-token-ttl-minutes:15}") long accessTokenTtlMinutes
    ) {
        this(objectMapper, Clock.systemUTC(), secret, accessTokenTtlMinutes);
    }

    JwtTokenProvider(ObjectMapper objectMapper, Clock clock, String secret, long accessTokenTtlMinutes) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
        }
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenTtlSeconds = accessTokenTtlMinutes * 60;
    }

    public String createAccessToken(User user, List<String> roles) {
        Instant now = Instant.now(clock);
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", String.valueOf(user.getId()));
        payload.put("email", user.getEmail());
        payload.put("roles", roles);
        payload.put("iss", ISSUER);
        payload.put("aud", AUDIENCE);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plusSeconds(accessTokenTtlSeconds).getEpochSecond());

        String unsignedToken = base64Json(header) + "." + base64Json(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public Optional<AuthenticatedPrincipal> validate(String token) {
        try {
            if (token == null || token.isBlank()) {
                return Optional.empty();
            }
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }

            Map<String, Object> header = objectMapper.readValue(URL_DECODER.decode(parts[0]), new TypeReference<>() {
            });
            if (!"HS256".equals(header.get("alg")) || !"JWT".equals(header.get("typ"))) {
                return Optional.empty();
            }

            String unsignedToken = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(unsignedToken).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                return Optional.empty();
            }

            Map<String, Object> payload = objectMapper.readValue(URL_DECODER.decode(parts[1]), new TypeReference<>() {
            });
            if (!ISSUER.equals(payload.get("iss")) || !AUDIENCE.equals(payload.get("aud"))) {
                return Optional.empty();
            }
            long expiresAt = ((Number) payload.get("exp")).longValue();
            if (Instant.now(clock).getEpochSecond() >= expiresAt) {
                return Optional.empty();
            }

            Long userId = Long.valueOf(String.valueOf(payload.get("sub")));
            String email = String.valueOf(payload.get("email"));
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) payload.get("roles");
            return Optional.of(new AuthenticatedPrincipal(userId, email, roles));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }

    private String base64Json(Map<String, Object> value) {
        try {
            return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize JWT", e);
        }
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return URL_ENCODER.encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign JWT", e);
        }
    }
}
