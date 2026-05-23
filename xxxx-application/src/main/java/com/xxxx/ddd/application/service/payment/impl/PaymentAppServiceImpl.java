package com.xxxx.ddd.application.service.payment.impl;

import com.xxxx.ddd.application.service.order.OrderAppException;
import com.xxxx.ddd.application.port.payment.PaymentGatewayPort;
import com.xxxx.ddd.application.service.payment.PaymentAppService;
import com.xxxx.ddd.application.service.payment.PaymentResultDTO;
import com.xxxx.ddd.domain.model.entity.Order;
import com.xxxx.ddd.domain.model.entity.Payment;
import com.xxxx.ddd.domain.model.enums.OrderStatus;
import com.xxxx.ddd.domain.model.enums.PaymentStatus;
import com.xxxx.ddd.domain.respository.ticketing.OrderRepository;
import com.xxxx.ddd.domain.respository.ticketing.PaymentRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class PaymentAppServiceImpl implements PaymentAppService {

    private final OrderRepository orderRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final PaymentGatewayPort paymentGateway;

    public PaymentAppServiceImpl(
            OrderRepository orderRepository,
            PaymentRecordRepository paymentRecordRepository,
            PaymentGatewayPort paymentGateway
    ) {
        this.orderRepository = orderRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.paymentGateway = paymentGateway;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultDTO mockPayment(Long actorUserId, boolean admin, Long orderId) {
        log.info("mockPayment: actorUserId={} admin={} orderId={}", actorUserId, admin, orderId);

        Order order = loadPendingOrder(actorUserId, admin, orderId);

        // Create payment record: INIT → SUCCESS immediately for mock
        String paymentId = UUID.randomUUID().toString();
        Payment payment = Payment.init(paymentId, order.getId(), actorUserId, order.getTotalAmount(), "MOCK");
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRecordRepository.save(payment);

        // Transition order to PAID
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        log.info("mockPayment: success paymentId={} orderId={}", paymentId, orderId);
        return PaymentResultDTO.success(paymentId, orderId, PaymentStatus.SUCCESS, OrderStatus.PAID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultDTO vnpayPayment(Long actorUserId, boolean admin, Long orderId) {
        log.info("vnpayPayment: actorUserId={} admin={} orderId={}", actorUserId, admin, orderId);

        Order order = loadPendingOrder(actorUserId, admin, orderId);

        // Create payment record with PENDING status
        String paymentId = UUID.randomUUID().toString();
        Payment payment = Payment.init(paymentId, order.getId(), actorUserId, order.getTotalAmount(), "VNPAY");
        payment.setStatus(PaymentStatus.PENDING);
        paymentRecordRepository.save(payment);

        // Build signed VNPAY URL
        String paymentUrl = paymentGateway.createPaymentUrl(payment);
        payment.setPaymentUrl(paymentUrl);
        paymentRecordRepository.save(payment);

        log.info("vnpayPayment: URL generated paymentId={} orderId={}", paymentId, orderId);
        return PaymentResultDTO.successWithUrl(paymentId, orderId, PaymentStatus.PENDING, OrderStatus.PENDING, paymentUrl);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultDTO vnpayCallback(Map<String, String> callbackParams) {
        log.info("vnpayCallback: params={}", callbackParams);

        // 1. Verify signature
        boolean signatureValid = paymentGateway.verifyCallback(callbackParams);
        if (!signatureValid) {
            log.warn("vnpayCallback: invalid signature");
            throw new OrderAppException("INVALID_SIGNATURE", "VNPAY callback signature is invalid");
        }

        // 2. Look up payment by vnp_TxnRef (which is our paymentId)
        String txnRef = callbackParams.get("vnp_TxnRef");
        if (txnRef == null || txnRef.isBlank()) {
            throw new OrderAppException("PAYMENT_NOT_FOUND", "Payment reference not found in callback");
        }

        Payment payment = paymentRecordRepository.findByPaymentId(txnRef)
                .orElseThrow(() -> new OrderAppException("PAYMENT_NOT_FOUND", "Payment not found"));

        // 3. Check if already processed
        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED) {
            log.info("vnpayCallback: payment already processed paymentId={} status={}", txnRef, payment.getStatus());
            Order order = orderRepository.findById(payment.getOrderId())
                    .orElseThrow(() -> new OrderAppException("ORDER_NOT_FOUND", "Order not found"));
            return PaymentResultDTO.success(payment.getPaymentId(), order.getId(), payment.getStatus(), order.getStatus());
        }

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new OrderAppException("ORDER_NOT_FOUND", "Order not found"));

        // 4. Check response code
        String responseCode = callbackParams.get("vnp_ResponseCode");
        String gatewayTransactionId = callbackParams.get("vnp_TransactionNo");

        if ("00".equals(responseCode)) {
            // Success
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setGatewayTransactionId(gatewayTransactionId);
            paymentRecordRepository.save(payment);

            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            log.info("vnpayCallback: payment success paymentId={} orderId={}", txnRef, order.getId());
            return PaymentResultDTO.success(payment.getPaymentId(), order.getId(), PaymentStatus.SUCCESS, OrderStatus.PAID);
        } else {
            // Failed
            payment.setStatus(PaymentStatus.FAILED);
            payment.setGatewayTransactionId(gatewayTransactionId);
            paymentRecordRepository.save(payment);

            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);

            log.info("vnpayCallback: payment failed paymentId={} orderId={} responseCode={}", txnRef, order.getId(), responseCode);
            return PaymentResultDTO.success(payment.getPaymentId(), order.getId(), PaymentStatus.FAILED, OrderStatus.PAYMENT_FAILED);
        }
    }

    private Order loadPendingOrder(Long actorUserId, boolean admin, Long orderId) {
        if (actorUserId == null || actorUserId <= 0) {
            throw new OrderAppException("USER_NOT_FOUND", "Authenticated user is required");
        }
        if (orderId == null || orderId <= 0) {
            throw new OrderAppException("ORDER_NOT_FOUND", "Order not found");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderAppException("ORDER_NOT_FOUND", "Order not found"));

        // Authorization: owner or ADMIN
        if (!admin && !order.getUserId().equals(actorUserId)) {
            throw new OrderAppException("ORDER_FORBIDDEN", "Order does not belong to user");
        }

        // Only PENDING orders can be paid
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderAppException("ORDER_NOT_PAYABLE", "Only pending orders can be paid");
        }

        return order;
    }
}
