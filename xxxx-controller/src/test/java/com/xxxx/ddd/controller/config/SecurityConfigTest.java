package com.xxxx.ddd.controller.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.ddd.application.service.auth.JwtTokenProvider;
import com.xxxx.ddd.domain.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigTest.SecurityProbeController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityConfigTest.TestJwtConfig.class})
@ContextConfiguration(classes = {
        SecurityConfigTest.SecurityProbeController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        SecurityConfigTest.TestJwtConfig.class
})
@TestPropertySource(properties = {
        "auth.jwt.secret=test-auth-secret-with-at-least-32-bytes-2026",
        "auth.jwt.access-token-ttl-minutes=15"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void authenticatedEndpointRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointRejectsUserRole() throws Exception {
        String userToken = tokenWithRole("USER");

        mockMvc.perform(get("/api/admin/probe").header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointAcceptsAdminRole() throws Exception {
        String adminToken = tokenWithRole("ADMIN");

        mockMvc.perform(get("/api/admin/probe").header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("admin-ok"));
    }

    private String tokenWithRole(String role) {
        User user = User.registerLocal(role.toLowerCase() + "@example.com", "hash", role + " User").setId(10L);
        return jwtTokenProvider.createAccessToken(user, List.of(role));
    }

    @RestController
    static class SecurityProbeController {

        @GetMapping("/api/admin/probe")
        String adminProbe() {
            return "admin-ok";
        }

        @GetMapping("/api/auth/me")
        String meProbe() {
            return "me-ok";
        }
    }

    @TestConfiguration
    static class TestJwtConfig {

        @Bean
        JwtTokenProvider jwtTokenProvider(ObjectMapper objectMapper) {
            return new JwtTokenProvider(objectMapper, "test-auth-secret-with-at-least-32-bytes-2026", 15);
        }
    }
}
