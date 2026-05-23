package com.xxxx.ddd.controller.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.ddd.application.model.OrderDTO;
import com.xxxx.ddd.application.model.OrderItemDTO;
import com.xxxx.ddd.application.model.command.CreateOrderCommand;
import com.xxxx.ddd.application.model.response.PlaceOrderResponse;
import com.xxxx.ddd.application.service.auth.JwtTokenProvider;
import com.xxxx.ddd.application.service.order.OrderAppException;
import com.xxxx.ddd.application.service.order.OrderAppService;
import com.xxxx.ddd.controller.config.JwtAuthenticationFilter;
import com.xxxx.ddd.controller.config.SecurityConfig;
import com.xxxx.ddd.domain.model.entity.User;
import com.xxxx.ddd.domain.model.enums.OrderStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, OrderControllerTest.TestJwtConfig.class})
@ContextConfiguration(classes = {
        OrderController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        OrderControllerTest.TestJwtConfig.class
})
@TestPropertySource(properties = {
        "auth.jwt.secret=test-auth-secret-with-at-least-32-bytes-2026",
        "auth.jwt.access-token-ttl-minutes=15"
})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private OrderAppService orderAppService;

    @Test
    void orderRoutesRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticketTypeId": 1,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void placeOrderReturnsCreatedForAuthenticatedUser() throws Exception {
        when(orderAppService.placeOrder(eq(10L), any(CreateOrderCommand.class))).thenReturn(PlaceOrderResponse.success(100L));

        mockMvc.perform(post("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithRole(10L, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticketTypeId": 1,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.orderId").value(100));
    }

    @Test
    void placeOrderMapsOutOfStockToConflict() throws Exception {
        when(orderAppService.placeOrder(eq(10L), any(CreateOrderCommand.class)))
                .thenReturn(PlaceOrderResponse.failed("OUT_OF_STOCK", "Ticket stock is not enough"));

        mockMvc.perform(post("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithRole(10L, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticketTypeId": 1,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result.code").value("OUT_OF_STOCK"));
    }

    @Test
    void getOrderMapsForbiddenOwnershipFailure() throws Exception {
        when(orderAppService.getOrder(10L, false, 200L))
                .thenThrow(new OrderAppException("ORDER_FORBIDDEN", "Order does not belong to user"));

        mockMvc.perform(get("/api/orders/200")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithRole(10L, "USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void adminCanCancelAnotherUsersOrder() throws Exception {
        when(orderAppService.cancelOrder(99L, true, 200L)).thenReturn(order(200L, 20L, OrderStatus.CANCELLED));

        mockMvc.perform(put("/api/orders/200/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithRole(99L, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("CANCELLED"));
    }

    private String tokenWithRole(Long userId, String role) {
        User user = User.registerLocal(role.toLowerCase() + userId + "@example.com", "hash", role + " User").setId(userId);
        return jwtTokenProvider.createAccessToken(user, List.of(role));
    }

    private static OrderDTO order(Long orderId, Long userId, OrderStatus status) {
        LocalDateTime now = LocalDateTime.parse("2026-06-01T10:00:00");
        return new OrderDTO(
                orderId,
                "ORD-" + userId + "-1",
                userId,
                status,
                BigDecimal.TEN,
                now,
                now,
                List.of(new OrderItemDTO(1L, 1L, 1, BigDecimal.TEN, BigDecimal.TEN))
        );
    }

    @TestConfiguration
    static class TestJwtConfig {

        @Bean
        JwtTokenProvider jwtTokenProvider(ObjectMapper objectMapper) {
            return new JwtTokenProvider(objectMapper, "test-auth-secret-with-at-least-32-bytes-2026", 15);
        }
    }
}
