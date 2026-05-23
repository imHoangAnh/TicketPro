package com.xxxx.ddd.application.service.payment;

import com.xxxx.ddd.domain.model.enums.OrderStatus;
import com.xxxx.ddd.domain.model.enums.PaymentStatus;

/**
 * DTO returned by payment operations.
 */
public record PaymentResultDTO(
        boolean success,
        String paymentId,
        Long orderId,
        PaymentStatus paymentStatus,
        OrderStatus orderStatus,
        String paymentUrl,
        String code,
        String message
) {
    public static PaymentResultDTO success(String paymentId, Long orderId, PaymentStatus paymentStatus, OrderStatus orderStatus) {
        return new PaymentResultDTO(true, paymentId, orderId, paymentStatus, orderStatus, null, null, null);
    }

    public static PaymentResultDTO successWithUrl(String paymentId, Long orderId, PaymentStatus paymentStatus, OrderStatus orderStatus, String paymentUrl) {
        return new PaymentResultDTO(true, paymentId, orderId, paymentStatus, orderStatus, paymentUrl, null, null);
    }

    public static PaymentResultDTO failed(String code, String message) {
        return new PaymentResultDTO(false, null, null, null, null, null, code, message);
    }
}
