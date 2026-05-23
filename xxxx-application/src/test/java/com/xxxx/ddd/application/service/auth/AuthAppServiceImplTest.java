package com.xxxx.ddd.application.service.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.ddd.application.model.auth.LoginCommand;
import com.xxxx.ddd.application.model.auth.RegisterCommand;
import com.xxxx.ddd.application.service.auth.impl.AuthAppServiceImpl;
import com.xxxx.ddd.domain.model.auth.RefreshTokenSession;
import com.xxxx.ddd.domain.model.entity.Role;
import com.xxxx.ddd.domain.model.entity.User;
import com.xxxx.ddd.domain.model.entity.UserRole;
import com.xxxx.ddd.domain.model.enums.RoleName;
import com.xxxx.ddd.domain.respository.auth.RefreshTokenRepository;
import com.xxxx.ddd.domain.respository.ticketing.RoleRepository;
import com.xxxx.ddd.domain.respository.ticketing.UserRepository;
import com.xxxx.ddd.domain.respository.ticketing.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthAppServiceImplTest {

    private InMemoryUserRepository users;
    private InMemoryRoleRepository roles;
    private InMemoryUserRoleRepository userRoles;
    private InMemoryRefreshTokenRepository refreshTokens;
    private AuthAppServiceImpl authAppService;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        roles = new InMemoryRoleRepository();
        userRoles = new InMemoryUserRoleRepository();
        refreshTokens = new InMemoryRefreshTokenRepository();

        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
                new ObjectMapper(),
                "test-auth-secret-with-at-least-32-bytes-2026",
                15
        );
        authAppService = new AuthAppServiceImpl(
                users,
                roles,
                userRoles,
                refreshTokens,
                new BCryptPasswordEncoder(),
                jwtTokenProvider,
                14
        );
    }

    @Test
    void registerHashesPasswordAssignsUserRoleAndStoresRefreshSession() {
        var session = authAppService.register(new RegisterCommand("Buyer@Example.com", "password123", "Buyer"));

        User savedUser = users.findByEmail("buyer@example.com").orElseThrow();
        assertThat(savedUser.getPasswordHash()).startsWith("$2a$");
        assertThat(userRoles.findByUserId(savedUser.getId())).hasSize(1);
        assertThat(session.response().accessToken()).isNotBlank();
        assertThat(session.response().user().roles()).containsExactly("USER");
        assertThat(refreshTokens.sessions).hasSize(1);
        assertThat(refreshTokens.sessions).doesNotContainKey(session.refreshToken());
    }

    @Test
    void loginRejectsWrongPassword() {
        authAppService.register(new RegisterCommand("buyer@example.com", "password123", "Buyer"));

        assertThatThrownBy(() -> authAppService.login(new LoginCommand("buyer@example.com", "wrong-password")))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void refreshRotatesTokenAndRejectsReusingOldRefreshToken() {
        var login = authAppService.register(new RegisterCommand("buyer@example.com", "password123", "Buyer"));
        String oldRefreshToken = login.refreshToken();

        var refreshed = authAppService.refresh(oldRefreshToken);

        assertThat(refreshed.refreshToken()).isNotEqualTo(oldRefreshToken);
        assertThat(refreshTokens.sessions).hasSize(1);
        assertThatThrownBy(() -> authAppService.refresh(oldRefreshToken))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Refresh token is invalid");
    }

    @Test
    void concurrentRefreshCanConsumeOldRefreshTokenOnlyOnce() throws Exception {
        var login = authAppService.register(new RegisterCommand("buyer@example.com", "password123", "Buyer"));
        String oldRefreshToken = login.refreshToken();

        var executor = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Boolean>> attempts = List.of(
                    () -> tryRefresh(oldRefreshToken),
                    () -> tryRefresh(oldRefreshToken)
            );

            List<Boolean> results = executor.invokeAll(attempts)
                    .stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .toList();

            assertThat(results).containsExactlyInAnyOrder(true, false);
            assertThat(refreshTokens.sessions).hasSize(1);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void logoutInvalidatesCurrentRefreshToken() {
        var login = authAppService.register(new RegisterCommand("buyer@example.com", "password123", "Buyer"));

        authAppService.logout(login.refreshToken());

        assertThat(refreshTokens.sessions).isEmpty();
    }

    private boolean tryRefresh(String refreshToken) {
        try {
            authAppService.refresh(refreshToken);
            return true;
        } catch (AuthException e) {
            return false;
        }
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final Map<Long, User> byId = new HashMap<>();
        private final Map<String, User> byEmail = new HashMap<>();
        private long nextId = 1;

        @Override
        public User save(User user) {
            if (user.getId() == null) {
                user.setId(nextId++);
            }
            byId.put(user.getId(), user);
            byEmail.put(user.getEmail(), user);
            return user;
        }

        @Override
        public Optional<User> findById(Long userId) {
            return Optional.ofNullable(byId.get(userId));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(byEmail.get(email));
        }
    }

    private static final class InMemoryRoleRepository implements RoleRepository {
        private final Map<Long, Role> byId = Map.of(
                1L, Role.of(RoleName.USER).setId(1L),
                2L, Role.of(RoleName.ADMIN).setId(2L)
        );

        @Override
        public Role save(Role role) {
            return role;
        }

        @Override
        public Optional<Role> findById(Long roleId) {
            return Optional.ofNullable(byId.get(roleId));
        }

        @Override
        public Optional<Role> findByName(RoleName name) {
            return byId.values().stream().filter(role -> role.getName() == name).findFirst();
        }
    }

    private static final class InMemoryUserRoleRepository implements UserRoleRepository {
        private final List<UserRole> userRoles = new ArrayList<>();

        @Override
        public UserRole save(UserRole userRole) {
            userRoles.add(userRole);
            return userRole;
        }

        @Override
        public List<UserRole> findByUserId(Long userId) {
            return userRoles.stream().filter(userRole -> userRole.getUserId().equals(userId)).toList();
        }
    }

    private static final class InMemoryRefreshTokenRepository implements RefreshTokenRepository {
        private final Map<String, RefreshTokenSession> sessions = new ConcurrentHashMap<>();

        @Override
        public void save(RefreshTokenSession session, Duration ttl) {
            sessions.put(session.getTokenHash(), session);
        }

        @Override
        public Optional<RefreshTokenSession> findByHash(String tokenHash) {
            return Optional.ofNullable(sessions.get(tokenHash));
        }

        @Override
        public Optional<RefreshTokenSession> consumeByHash(String tokenHash) {
            return Optional.ofNullable(sessions.remove(tokenHash));
        }

        @Override
        public void deleteByHash(String tokenHash) {
            sessions.remove(tokenHash);
        }
    }

}
