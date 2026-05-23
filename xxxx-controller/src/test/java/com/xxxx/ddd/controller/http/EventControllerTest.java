package com.xxxx.ddd.controller.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.ddd.application.model.EventDetailDTO;
import com.xxxx.ddd.application.model.EventSummaryDTO;
import com.xxxx.ddd.application.model.TicketTypeDTO;
import com.xxxx.ddd.application.model.command.CreateEventCommand;
import com.xxxx.ddd.application.service.auth.JwtTokenProvider;
import com.xxxx.ddd.application.service.event.EventAppException;
import com.xxxx.ddd.application.service.event.EventAppService;
import com.xxxx.ddd.controller.config.JwtAuthenticationFilter;
import com.xxxx.ddd.controller.config.SecurityConfig;
import com.xxxx.ddd.domain.model.entity.User;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PublicEventController.class, AdminEventController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, EventControllerTest.TestJwtConfig.class})
@ContextConfiguration(classes = {
        PublicEventController.class,
        AdminEventController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        EventControllerTest.TestJwtConfig.class
})
@TestPropertySource(properties = {
        "auth.jwt.secret=test-auth-secret-with-at-least-32-bytes-2026",
        "auth.jwt.access-token-ttl-minutes=15"
})
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private EventAppService eventAppService;

    @Test
    void publicListEventsDoesNotRequireAuthentication() throws Exception {
        when(eventAppService.listActiveEvents()).thenReturn(List.of(new EventSummaryDTO(
                1L,
                "Concert",
                "Live",
                "Venue",
                LocalDateTime.parse("2026-06-01T19:00:00"),
                LocalDateTime.parse("2026-06-01T21:00:00"),
                true
        )));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].title").value("Concert"));
    }

    @Test
    void publicDetailReturnsNotFoundForInactiveOrMissingEvent() throws Exception {
        when(eventAppService.getActiveEventDetail(404L)).thenThrow(new EventAppException("Event not found"));

        mockMvc.perform(get("/api/events/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void adminCreateEventRequiresAdminRole() throws Exception {
        String payload = """
                {
                  "title": "Concert",
                  "description": "Live",
                  "venue": "Venue",
                  "startAt": "2026-06-01T19:00:00",
                  "endAt": "2026-06-01T21:00:00",
                  "active": true
                }
                """;

        mockMvc.perform(post("/api/admin/events").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/admin/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithRole("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCreateEventAcceptsAdminRole() throws Exception {
        LocalDateTime startAt = LocalDateTime.parse("2026-06-01T19:00:00");
        when(eventAppService.createEvent(any(CreateEventCommand.class))).thenReturn(new EventDetailDTO(
                1L,
                "Concert",
                "Live",
                "Venue",
                startAt,
                startAt.plusHours(2),
                true,
                List.of(new TicketTypeDTO(1L, 1L, "Standard", "Seat", BigDecimal.valueOf(10), 100, 100, true))
        ));

        mockMvc.perform(post("/api/admin/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithRole("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Concert",
                                  "description": "Live",
                                  "venue": "Venue",
                                  "startAt": "2026-06-01T19:00:00",
                                  "endAt": "2026-06-01T21:00:00",
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.ticketTypes[0].name").value("Standard"));
    }

    @Test
    void adminCreateTicketTypeMapsMissingEventToNotFound() throws Exception {
        when(eventAppService.createTicketType(eq(99L), any())).thenThrow(new EventAppException("Event not found"));

        mockMvc.perform(post("/api/admin/events/99/ticket-types")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithRole("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "VIP",
                                  "description": "Front section",
                                  "price": 100.00,
                                  "stockInitial": 10
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    private String tokenWithRole(String role) {
        User user = User.registerLocal(role.toLowerCase() + "@example.com", "hash", role + " User").setId(10L);
        return jwtTokenProvider.createAccessToken(user, List.of(role));
    }

    @TestConfiguration
    static class TestJwtConfig {

        @Bean
        JwtTokenProvider jwtTokenProvider(ObjectMapper objectMapper) {
            return new JwtTokenProvider(objectMapper, "test-auth-secret-with-at-least-32-bytes-2026", 15);
        }
    }
}
