package com.xxxx.ddd.controller.http;

import com.xxxx.ddd.application.model.auth.AuthenticatedPrincipal;
import com.xxxx.ddd.application.service.order.OrderAppException;
import com.xxxx.ddd.application.service.payment.PaymentAppService;
import com.xxxx.ddd.application.service.payment.PaymentResultDTO;
import com.xxxx.ddd.controller.model.enums.ResultUtil;
import com.xxxx.ddd.controller.model.vo.ResultMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

/**
 * Payment endpoints for E06.
 * The accepted API contract is:
 * <ul>
 *     <li>POST /api/payments/{orderId}/mock-success — instant mock payment</li>
 *     <li>POST /api/payments/{orderId}/vnpay — returns VNPAY redirect URL</li>
 *     <li>GET  /api/payments/vnpay/callback — VNPAY browser redirect callback</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/payments")
@Slf4j
public class ApiPaymentController {

    private final PaymentAppService paymentAppService;

    public ApiPaymentController(PaymentAppService paymentAppService) {
        this.paymentAppService = paymentAppService;
    }

    @PostMapping("/{orderId}/mock-success")
    public ResponseEntity<ResultMessage<PaymentResultDTO>> mockPayment(
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        try {
            AuthenticatedPrincipal principal = principal(authentication);
            PaymentResultDTO result = paymentAppService.mockPayment(
                    principal.userId(),
                    isAdmin(principal),
                    orderId
            );
            return ResponseEntity.ok(ResultUtil.data(result));
        } catch (OrderAppException e) {
            return error(e);
        }
    }

    @PostMapping("/{orderId}/vnpay")
    public ResponseEntity<ResultMessage<PaymentResultDTO>> vnpayPayment(
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        try {
            AuthenticatedPrincipal principal = principal(authentication);
            PaymentResultDTO result = paymentAppService.vnpayPayment(
                    principal.userId(),
                    isAdmin(principal),
                    orderId
            );
            return ResponseEntity.ok(ResultUtil.data(result));
        } catch (OrderAppException e) {
            return error(e);
        }
    }

    /**
     * VNPAY callback endpoint. This is called by the user's browser after VNPAY
     * redirects back. It is a public endpoint (no authentication required)
     * because the user's browser has been redirected away from our domain.
     * Security is provided by VNPAY signature verification.
     */
    @GetMapping("/vnpay/callback")
    public ResponseEntity<?> vnpayCallback(@RequestParam Map<String, String> params) {
        try {
            PaymentResultDTO result = paymentAppService.vnpayCallback(params);
            // Redirect to frontend with result
            String redirectUrl = buildFrontendRedirectUrl(result);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
        } catch (OrderAppException e) {
            log.warn("vnpayCallback: failed code={} message={}", e.code(), e.getMessage());
            String errorRedirect = "http://localhost:5173/payment-result?success=false&error=" + e.code();
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(errorRedirect))
                    .build();
        }
    }

    private String buildFrontendRedirectUrl(PaymentResultDTO result) {
        boolean success = result.paymentStatus() != null
                && result.paymentStatus() == com.xxxx.ddd.domain.model.enums.PaymentStatus.SUCCESS;
        return "http://localhost:5173/payment-result"
                + "?success=" + success
                + "&orderId=" + result.orderId()
                + "&paymentId=" + result.paymentId();
    }

    private static AuthenticatedPrincipal principal(Authentication authentication) {
        return (AuthenticatedPrincipal) authentication.getPrincipal();
    }

    private static boolean isAdmin(AuthenticatedPrincipal principal) {
        return principal.roles().contains("ADMIN");
    }

    private static <T> ResponseEntity<ResultMessage<T>> error(OrderAppException e) {
        HttpStatus status = statusForFailureCode(e.code());
        return ResponseEntity.status(status).body(ResultUtil.error(status.value(), e.getMessage()));
    }

    private static HttpStatus statusForFailureCode(String code) {
        if ("ORDER_FORBIDDEN".equals(code)) {
            return HttpStatus.FORBIDDEN;
        }
        if ("ORDER_NOT_FOUND".equals(code) || "PAYMENT_NOT_FOUND".equals(code)) {
            return HttpStatus.NOT_FOUND;
        }
        if ("ORDER_NOT_PAYABLE".equals(code)) {
            return HttpStatus.CONFLICT;
        }
        if ("INVALID_SIGNATURE".equals(code)) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.BAD_REQUEST;
    }
}
