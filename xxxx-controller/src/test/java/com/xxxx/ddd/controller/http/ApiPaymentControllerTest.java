package com.xxxx.ddd.controller.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.ddd.application.service.auth.JwtTokenProvider;
import com.xxxx.ddd.application.service.order.OrderAppException;
import com.xxxx.ddd.application.service.payment.PaymentAppService;
import com.xxxx.ddd.application.service.payment.PaymentResultDTO;
import com.xxxx.ddd.controller.config.JwtAuthenticationFilter;
import com.xxxx.ddd.controller.config.SecurityConfig;
import com.xxxx.ddd.domain.model.entity.User;
import com.xxxx.ddd.domain.model.enums.OrderStatus;
import com.xxxx.ddd.domain.model.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApiPaymentController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ApiPaymentControllerTest.TestJwtConfig.class})
@ContextConfiguration(classes = {
        ApiPaymentController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        ApiPaymentControllerTest.TestJwtConfig.class
})
@TestPropertySource(properties = {
        "auth.jwt.secret=test-auth-secret-with-at-least-32-bytes-2026",
        "auth.jwt.access-token-ttl-minutes=15"
})
class ApiPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private PaymentAppService paymentAppService;

    // ---- Authentication Tests ----

    @Test
    void mockPaymentRequiresAuth() throws Exception {
        mockMvc.perform(post("/api/payments/100/mock-success"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void vnpayPaymentRequiresAuth() throws Exception {
        mockMvc.perform(post("/api/payments/100/vnpay"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void vnpayCallbackDoesNotRequireAuth() throws Exception {
        when(paymentAppService.vnpayCallback(any())).thenThrow(
                new OrderAppException("INVALID_SIGNATURE", "Invalid signature"));

        // Callback should be accessible without auth (302 redirect on error)
        mockMvc.perform(get("/api/payments/vnpay/callback")
                        .param("vnp_TxnRef", "test")
                        .param("vnp_SecureHash", "invalid"))
                .andExpect(status().isFound());
    }

    // ---- Mock Payment Route Tests ----

    @Test
    void mockPaymentReturnsOkOnSuccess() throws Exception {
        PaymentResultDTO result = PaymentResultDTO.success("pay-1", 100L, PaymentStatus.SUCCESS, OrderStatus.PAID);
        when(paymentAppService.mockPayment(eq(10L), eq(false), eq(100L))).thenReturn(result);

        mockMvc.perform(post("/api/payments/100/mock-success")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithRole(10L, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.paymentId").value("pay-1"))
                .andExpect(jsonPath("$.result.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.result.orderStatus").value("PAID"));
    }

    @Test
    void mockPaymentReturnsNotFoundForMissingOrder() throws Exception {
        when(paymentAppService.mockPayment(eq(10L), eq(false), eq(999L)))
                .thenThrow(new OrderAppException("ORDER_NOT_FOUND", "Order not found"));

        mockMvc.perform(post("/api/payments/999/mock-success")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithRole(10L, "USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void mockPaymentReturnsForbiddenForWrongUser() throws Exception {
        when(paymentAppService.mockPayment(eq(10L), eq(false), eq(100L)))
                .thenThrow(new OrderAppException("ORDER_FORBIDDEN", "Order does not belong to user"));

        mockMvc.perform(post("/api/payments/100/mock-success")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithRole(10L, "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void mockPaymentReturnsConflictForNonPendingOrder() throws Exception {
        when(paymentAppService.mockPayment(eq(10L), eq(false), eq(100L)))
                .thenThrow(new OrderAppException("ORDER_NOT_PAYABLE", "Only pending orders can be paid"));

        mockMvc.perform(post("/api/payments/100/mock-success")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithRole(10L, "USER")))
                .andExpect(status().isConflict());
    }

    // ---- VNPAY Init Route Tests ----

    @Test
    void vnpayPaymentReturnsOkWithPaymentUrl() throws Exception {
        PaymentResultDTO result = PaymentResultDTO.successWithUrl(
                "pay-2", 100L, PaymentStatus.PENDING, OrderStatus.PENDING,
                "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?test=1");
        when(paymentAppService.vnpayPayment(eq(10L), eq(false), eq(100L))).thenReturn(result);

        mockMvc.perform(post("/api/payments/100/vnpay")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithRole(10L, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.paymentUrl").exists())
                .andExpect(jsonPath("$.result.paymentStatus").value("PENDING"));
    }

    // ---- VNPAY Callback Route Tests ----

    @Test
    void vnpayCallbackRedirectsOnSuccess() throws Exception {
        PaymentResultDTO result = PaymentResultDTO.success("pay-cb", 100L, PaymentStatus.SUCCESS, OrderStatus.PAID);
        when(paymentAppService.vnpayCallback(any())).thenReturn(result);

        mockMvc.perform(get("/api/payments/vnpay/callback")
                        .param("vnp_TxnRef", "pay-cb")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_SecureHash", "validhash"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("success=true")));
    }

    @Test
    void vnpayCallbackRedirectsOnFailure() throws Exception {
        PaymentResultDTO result = PaymentResultDTO.success("pay-fail", 100L, PaymentStatus.FAILED, OrderStatus.PAYMENT_FAILED);
        when(paymentAppService.vnpayCallback(any())).thenReturn(result);

        mockMvc.perform(get("/api/payments/vnpay/callback")
                        .param("vnp_TxnRef", "pay-fail")
                        .param("vnp_ResponseCode", "24")
                        .param("vnp_SecureHash", "validhash"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("success=false")));
    }

    // ---- Admin Tests ----

    @Test
    void adminCanMockPayForAnyOrder() throws Exception {
        PaymentResultDTO result = PaymentResultDTO.success("pay-admin", 100L, PaymentStatus.SUCCESS, OrderStatus.PAID);
        when(paymentAppService.mockPayment(eq(99L), eq(true), eq(100L))).thenReturn(result);

        mockMvc.perform(post("/api/payments/100/mock-success")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithRole(99L, "ADMIN")))
                .andExpect(status().isOk());
    }

    private String tokenWithRole(Long userId, String role) {
        User user = User.registerLocal(role.toLowerCase() + userId + "@example.com", "hash", role + " User").setId(userId);
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
