package com.xxxx.ddd.controller.http;

import com.xxxx.ddd.controller.model.enums.ResultUtil;
import com.xxxx.ddd.controller.model.vo.ResultMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Legacy payment controller at /payment.
 * Restricted to ADMIN by SecurityConfig until fully replaced.
 * E06 payment behavior is served by {@link ApiPaymentController} at /api/payments.
 */
@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    @PostMapping("/create")
    public ResultMessage<String> paymentOrder() {
        log.warn("Legacy /payment/create endpoint called. Use /api/payments instead.");
        return ResultUtil.error(410, "This endpoint is deprecated. Use /api/payments/{orderId}/mock-success or /api/payments/{orderId}/vnpay instead.");
    }
}
