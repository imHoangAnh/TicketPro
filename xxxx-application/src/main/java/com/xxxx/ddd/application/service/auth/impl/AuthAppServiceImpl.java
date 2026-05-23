package com.xxxx.ddd.application.service.auth.impl;

import com.xxxx.ddd.application.model.auth.AuthTokenResponse;
import com.xxxx.ddd.application.model.auth.AuthUserResponse;
import com.xxxx.ddd.application.model.auth.IssuedAuthSession;
import com.xxxx.ddd.application.model.auth.LoginCommand;
import com.xxxx.ddd.application.model.auth.RegisterCommand;
import com.xxxx.ddd.application.service.auth.AuthAppService;
import com.xxxx.ddd.application.service.auth.AuthException;
import com.xxxx.ddd.application.service.auth.JwtTokenProvider;
import com.xxxx.ddd.domain.model.auth.RefreshTokenSession;
import com.xxxx.ddd.domain.model.entity.Role;
import com.xxxx.ddd.domain.model.entity.User;
import com.xxxx.ddd.domain.model.entity.UserRole;
import com.xxxx.ddd.domain.model.enums.RoleName;
import com.xxxx.ddd.domain.respository.auth.RefreshTokenRepository;
import com.xxxx.ddd.domain.respository.ticketing.RoleRepository;
import com.xxxx.ddd.domain.respository.ticketing.UserRepository;
import com.xxxx.ddd.domain.respository.ticketing.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class AuthAppServiceImpl implements AuthAppService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder TOKEN_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final Duration refreshTokenTtl;

    public AuthAppServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            @Value("${auth.refresh-token.ttl-days:14}") long refreshTokenTtlDays
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenTtl = Duration.ofDays(refreshTokenTtlDays);
    }

    @Override
    @Transactional
    public IssuedAuthSession register(RegisterCommand command) {
        String email = normalizeEmail(command.email());
        validatePassword(command.password());
        String fullName = normalizeFullName(command.fullName());

        if (userRepository.findByEmail(email).isPresent()) {
            throw new AuthException("Email already registered");
        }

        User user = userRepository.save(User.registerLocal(email, passwordEncoder.encode(command.password()), fullName));
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new AuthException("Default USER role is missing"));
        userRoleRepository.save(UserRole.assign(user.getId(), userRole.getId()));

        log.info("Registered local user userId={}", user.getId());
        return issueSession(user);
    }

    @Override
    @Transactional(readOnly = true)
    public IssuedAuthSession login(LoginCommand command) {
        String email = normalizeEmail(command.email());
        User user = userRepository.findByEmail(email)
                .filter(User::isEnabled)
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new AuthException("Invalid email or password");
        }

        log.info("User login succeeded userId={}", user.getId());
        return issueSession(user);
    }

    @Override
    @Transactional
    public IssuedAuthSession refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException("Refresh token is missing");
        }

        String oldHash = hashToken(refreshToken);
        RefreshTokenSession oldSession = refreshTokenRepository.consumeByHash(oldHash)
                .orElseThrow(() -> new AuthException("Refresh token is invalid"));

        Instant now = Instant.now();
        if (oldSession.isExpired(now)) {
            throw new AuthException("Refresh token is expired");
        }

        User user = userRepository.findById(oldSession.getUserId())
                .filter(User::isEnabled)
                .orElseThrow(() -> new AuthException("User is disabled or missing"));

        log.info("Refresh token rotated userId={}", user.getId());
        return issueSession(user);
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.deleteByHash(hashToken(refreshToken));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthUserResponse me(Long userId) {
        User user = userRepository.findById(userId)
                .filter(User::isEnabled)
                .orElseThrow(() -> new AuthException("User is disabled or missing"));
        return toUserResponse(user, loadRoleNames(user.getId()));
    }

    private IssuedAuthSession issueSession(User user) {
        List<String> roles = loadRoleNames(user.getId());
        String accessToken = jwtTokenProvider.createAccessToken(user, roles);
        String refreshToken = newOpaqueToken();
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(refreshTokenTtl);
        String tokenHash = hashToken(refreshToken);

        RefreshTokenSession session = new RefreshTokenSession()
                .setTokenHash(tokenHash)
                .setUserId(user.getId())
                .setEmail(user.getEmail())
                .setRoles(roles)
                .setIssuedAt(issuedAt)
                .setExpiresAt(expiresAt);
        refreshTokenRepository.save(session, refreshTokenTtl);

        AuthTokenResponse response = new AuthTokenResponse(
                accessToken,
                jwtTokenProvider.getAccessTokenTtlSeconds(),
                toUserResponse(user, roles)
        );
        return new IssuedAuthSession(response, refreshToken);
    }

    private List<String> loadRoleNames(Long userId) {
        return userRoleRepository.findByUserId(userId)
                .stream()
                .map(UserRole::getRoleId)
                .map(roleId -> roleRepository.findById(roleId)
                        .map(Role::getName)
                        .orElseThrow(() -> new AuthException("Assigned role is missing")))
                .map(Enum::name)
                .distinct()
                .toList();
    }

    private static AuthUserResponse toUserResponse(User user, List<String> roles) {
        return new AuthUserResponse(user.getId(), user.getEmail(), user.getFullName(), roles);
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new AuthException("Email is invalid");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new AuthException("Full name is required");
        }
        return fullName.trim();
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new AuthException("Password must be at least 8 characters");
        }
    }

    private static String newOpaqueToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return TOKEN_ENCODER.encodeToString(bytes);
    }

    private static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return TOKEN_ENCODER.encodeToString(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash token", e);
        }
    }
}
