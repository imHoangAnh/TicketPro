package com.xxxx.ddd.controller.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.ddd.application.model.auth.AuthTokenResponse;
import com.xxxx.ddd.application.model.auth.AuthUserResponse;
import com.xxxx.ddd.application.model.auth.IssuedAuthSession;
import com.xxxx.ddd.application.model.auth.LoginCommand;
import com.xxxx.ddd.application.service.auth.AuthAppService;
import com.xxxx.ddd.application.service.auth.AuthException;
import com.xxxx.ddd.application.service.auth.JwtTokenProvider;
import com.xxxx.ddd.controller.config.JwtAuthenticationFilter;
import com.xxxx.ddd.controller.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, AuthControllerTest.TestJwtConfig.class})
@ContextConfiguration(classes = {
        AuthController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        AuthControllerTest.TestJwtConfig.class
})
@TestPropertySource(properties = {
        "auth.jwt.secret=test-auth-secret-with-at-least-32-bytes-2026",
        "auth.jwt.access-token-ttl-minutes=15",
        "auth.refresh-token.cookie-name=refresh_token",
        "auth.refresh-token.ttl-days=14",
        "auth.refresh-token.cookie-secure=false",
        "auth.refresh-token.cookie-same-site=Lax"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthAppService authAppService;

    @Test
    void loginSetsHttpOnlyRefreshCookieAndReturnsAccessToken() throws Exception {
        var user = new AuthUserResponse(1L, "user@example.com", "User", List.of("USER"));
        var tokenResponse = new AuthTokenResponse("access-token", 900, user);
        when(authAppService.login(any(LoginCommand.class)))
                .thenReturn(new IssuedAuthSession(tokenResponse, "refresh-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.accessToken").value("access-token"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=refresh-token")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")));
    }

    @Test
    void invalidLoginReturnsHttpUnauthorized() throws Exception {
        when(authAppService.login(any(LoginCommand.class)))
                .thenThrow(new AuthException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void refreshRequiresRequestGuardHeader() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "old-refresh-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void refreshRotatesRefreshCookie() throws Exception {
        var user = new AuthUserResponse(1L, "user@example.com", "User", List.of("USER"));
        var tokenResponse = new AuthTokenResponse("new-access-token", 900, user);
        when(authAppService.refresh("old-refresh-token"))
                .thenReturn(new IssuedAuthSession(tokenResponse, "new-refresh-token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.accessToken").value("new-access-token"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=new-refresh-token")));
    }

    @Test
    void logoutClearsRefreshCookieWithoutBearerToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=;")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }

    @Test
    void logoutRejectsMissingRequestGuardHeader() throws Exception {
        doThrow(new AuthException("Should not reach service")).when(authAppService).logout(anyString());

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "refresh-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @TestConfiguration
    static class TestJwtConfig {

        @Bean
        JwtTokenProvider jwtTokenProvider(ObjectMapper objectMapper) {
            return new JwtTokenProvider(objectMapper, "test-auth-secret-with-at-least-32-bytes-2026", 15);
        }
    }
}
