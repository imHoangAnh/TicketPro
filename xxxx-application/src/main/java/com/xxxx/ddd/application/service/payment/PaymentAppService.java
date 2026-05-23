package com.xxxx.ddd.application.service.payment;

import com.xxxx.ddd.application.service.order.OrderAppException;

import java.util.Map;

public interface PaymentAppService {

    /**
     * Process a mock instant payment for the given order.
     * Creates a payment record with method MOCK, transitions it to SUCCESS,
     * and marks the order as PAID.
     *
     * @param actorUserId the authenticated user's ID
     * @param admin       true if the caller has ADMIN role
     * @param orderId     the order to pay for
     * @return a DTO with payment details
     * @throws OrderAppException if order not found, not owned, or not PENDING
     */
    PaymentResultDTO mockPayment(Long actorUserId, boolean admin, Long orderId);

    /**
     * Initiate a VNPAY sandbox payment for the given order.
     * Creates a payment record with method VNPAY and status PENDING,
     * builds a signed VNPAY redirect URL, and returns it.
     *
     * @param actorUserId the authenticated user's ID
     * @param admin       true if the caller has ADMIN role
     * @param orderId     the order to pay for
     * @return a DTO containing the VNPAY redirect URL
     * @throws OrderAppException if order not found, not owned, or not PENDING
     */
    PaymentResultDTO vnpayPayment(Long actorUserId, boolean admin, Long orderId);

    /**
     * Handle the VNPAY callback with signed parameters.
     * Verifies the signature, updates payment and order status accordingly.
     *
     * @param callbackParams all query parameters from the VNPAY callback URL
     * @return a DTO with the payment result
     * @throws OrderAppException if signature invalid or payment not found
     */
    PaymentResultDTO vnpayCallback(Map<String, String> callbackParams);
}
