package com.xxxx.ddd.controller.http;

import com.xxxx.ddd.application.model.auth.AuthTokenResponse;
import com.xxxx.ddd.application.model.auth.AuthUserResponse;
import com.xxxx.ddd.application.model.auth.AuthenticatedPrincipal;
import com.xxxx.ddd.application.model.auth.IssuedAuthSession;
import com.xxxx.ddd.application.model.auth.LoginCommand;
import com.xxxx.ddd.application.model.auth.RegisterCommand;
import com.xxxx.ddd.application.service.auth.AuthAppService;
import com.xxxx.ddd.application.service.auth.AuthException;
import com.xxxx.ddd.controller.dto.auth.LoginRequest;
import com.xxxx.ddd.controller.dto.auth.RegisterRequest;
import com.xxxx.ddd.controller.model.enums.ResultUtil;
import com.xxxx.ddd.controller.model.vo.ResultMessage;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthAppService authAppService;
    private final String refreshCookieName;
    private final boolean refreshCookieSecure;
    private final String refreshCookieSameSite;
    private final Duration refreshCookieMaxAge;

    public AuthController(
            AuthAppService authAppService,
            @Value("${auth.refresh-token.cookie-name:refresh_token}") String refreshCookieName,
            @Value("${auth.refresh-token.cookie-secure:false}") boolean refreshCookieSecure,
            @Value("${auth.refresh-token.cookie-same-site:Lax}") String refreshCookieSameSite,
            @Value("${auth.refresh-token.ttl-days:14}") long refreshTokenTtlDays
    ) {
        this.authAppService = authAppService;
        this.refreshCookieName = refreshCookieName;
        this.refreshCookieSecure = refreshCookieSecure;
        this.refreshCookieSameSite = refreshCookieSameSite;
        this.refreshCookieMaxAge = Duration.ofDays(refreshTokenTtlDays);
    }

    @PostMapping("/register")
    public ResponseEntity<ResultMessage<AuthTokenResponse>> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        try {
            IssuedAuthSession session = authAppService.register(new RegisterCommand(
                    request.getEmail(),
                    request.getPassword(),
                    request.getFullName()
            ));
            setRefreshCookie(response, session.refreshToken(), refreshCookieMaxAge);
            return ResponseEntity.ok(ResultUtil.data(session.response()));
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultUtil.error(400, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResultMessage<AuthTokenResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            IssuedAuthSession session = authAppService.login(new LoginCommand(request.getEmail(), request.getPassword()));
            setRefreshCookie(response, session.refreshToken(), refreshCookieMaxAge);
            return ResponseEntity.ok(ResultUtil.data(session.response()));
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResultUtil.error(401, e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResultMessage<AuthTokenResponse>> refresh(
            @CookieValue(name = "${auth.refresh-token.cookie-name:refresh_token}", required = false) String refreshToken,
            @RequestHeader(name = "X-Requested-With", required = false) String requestedWith,
            HttpServletResponse response
    ) {
        try {
            assertCookieMutationGuard(requestedWith);
            IssuedAuthSession session = authAppService.refresh(refreshToken);
            setRefreshCookie(response, session.refreshToken(), refreshCookieMaxAge);
            return ResponseEntity.ok(ResultUtil.data(session.response()));
        } catch (AuthException e) {
            clearRefreshCookie(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResultUtil.error(401, e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ResultMessage<Boolean>> logout(
            @CookieValue(name = "${auth.refresh-token.cookie-name:refresh_token}", required = false) String refreshToken,
            @RequestHeader(name = "X-Requested-With", required = false) String requestedWith,
            HttpServletResponse response
    ) {
        try {
            assertCookieMutationGuard(requestedWith);
            authAppService.logout(refreshToken);
            clearRefreshCookie(response);
            return ResponseEntity.ok(ResultUtil.data(true));
        } catch (AuthException e) {
            clearRefreshCookie(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResultUtil.error(401, e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ResultMessage<AuthUserResponse>> me(Authentication authentication) {
        AuthenticatedPrincipal principal = (AuthenticatedPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ResultUtil.data(authAppService.me(principal.userId())));
    }

    private void setRefreshCookie(HttpServletResponse response, String refreshToken, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, refreshToken)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        setRefreshCookie(response, "", Duration.ZERO);
    }

    private static void assertCookieMutationGuard(String requestedWith) {
        if (!"XMLHttpRequest".equals(requestedWith)) {
            throw new AuthException("Missing auth request guard header");
        }
    }
}
