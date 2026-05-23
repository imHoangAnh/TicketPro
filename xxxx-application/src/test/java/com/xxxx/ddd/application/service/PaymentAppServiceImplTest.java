package com.xxxx.ddd.application.service;

import com.xxxx.ddd.application.port.payment.PaymentGatewayPort;
import com.xxxx.ddd.application.service.order.OrderAppException;
import com.xxxx.ddd.application.service.payment.PaymentAppService;
import com.xxxx.ddd.application.service.payment.PaymentResultDTO;
import com.xxxx.ddd.application.service.payment.impl.PaymentAppServiceImpl;
import com.xxxx.ddd.domain.model.entity.Order;
import com.xxxx.ddd.domain.model.entity.Payment;
import com.xxxx.ddd.domain.model.enums.OrderStatus;
import com.xxxx.ddd.domain.model.enums.PaymentStatus;
import com.xxxx.ddd.domain.respository.ticketing.OrderRepository;
import com.xxxx.ddd.domain.respository.ticketing.PaymentRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentAppServiceImplTest {

    private OrderRepository orderRepository;
    private PaymentRecordRepository paymentRecordRepository;
    private PaymentGatewayPort paymentGateway;
    private PaymentAppService paymentAppService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        paymentRecordRepository = mock(PaymentRecordRepository.class);
        paymentGateway = mock(PaymentGatewayPort.class);
        paymentAppService = new PaymentAppServiceImpl(orderRepository, paymentRecordRepository, paymentGateway);
    }

    private Order pendingOrder(Long userId) {
        Order order = Order.createPending(userId, "ORD-1-" + System.currentTimeMillis(), BigDecimal.valueOf(500000));
        order.setId(100L);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    // ---- Mock Payment Tests ----

    @Test
    void mockPayment_success() {
        Order order = pendingOrder(1L);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(paymentRecordRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResultDTO result = paymentAppService.mockPayment(1L, false, 100L);

        assertTrue(result.success());
        assertEquals(PaymentStatus.SUCCESS, result.paymentStatus());
        assertEquals(OrderStatus.PAID, result.orderStatus());
        assertEquals(100L, result.orderId());
        assertNotNull(result.paymentId());

        // Verify payment saved with MOCK method and SUCCESS status
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRecordRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        assertEquals("MOCK", savedPayment.getMethod());
        assertEquals(PaymentStatus.SUCCESS, savedPayment.getStatus());
    }

    @Test
    void mockPayment_adminCanPayForAnyUser() {
        Order order = pendingOrder(99L); // belongs to user 99
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(paymentRecordRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResultDTO result = paymentAppService.mockPayment(1L, true, 100L);
        assertTrue(result.success());
    }

    @Test
    void mockPayment_orderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        OrderAppException ex = assertThrows(OrderAppException.class,
                () -> paymentAppService.mockPayment(1L, false, 999L));
        assertEquals("ORDER_NOT_FOUND", ex.code());
    }

    @Test
    void mockPayment_orderNotOwned() {
        Order order = pendingOrder(99L);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        OrderAppException ex = assertThrows(OrderAppException.class,
                () -> paymentAppService.mockPayment(1L, false, 100L));
        assertEquals("ORDER_FORBIDDEN", ex.code());
    }

    @Test
    void mockPayment_orderNotPending() {
        Order order = pendingOrder(1L);
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        OrderAppException ex = assertThrows(OrderAppException.class,
                () -> paymentAppService.mockPayment(1L, false, 100L));
        assertEquals("ORDER_NOT_PAYABLE", ex.code());
    }

    @Test
    void mockPayment_orderCancelled() {
        Order order = pendingOrder(1L);
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        OrderAppException ex = assertThrows(OrderAppException.class,
                () -> paymentAppService.mockPayment(1L, false, 100L));
        assertEquals("ORDER_NOT_PAYABLE", ex.code());
    }

    // ---- VNPAY Payment Tests ----

    @Test
    void vnpayPayment_success() {
        Order order = pendingOrder(1L);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(paymentRecordRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentGateway.createPaymentUrl(any(Payment.class)))
                .thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?test=1");

        PaymentResultDTO result = paymentAppService.vnpayPayment(1L, false, 100L);

        assertTrue(result.success());
        assertEquals(PaymentStatus.PENDING, result.paymentStatus());
        assertEquals(OrderStatus.PENDING, result.orderStatus());
        assertNotNull(result.paymentUrl());
        assertTrue(result.paymentUrl().contains("sandbox.vnpayment.vn"));
    }

    @Test
    void vnpayPayment_orderNotPending() {
        Order order = pendingOrder(1L);
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        OrderAppException ex = assertThrows(OrderAppException.class,
                () -> paymentAppService.vnpayPayment(1L, false, 100L));
        assertEquals("ORDER_NOT_PAYABLE", ex.code());
    }

    // ---- VNPAY Callback Tests ----

    @Test
    void vnpayCallback_success() {
        Payment payment = Payment.init("pay-123", 100L, 1L, BigDecimal.valueOf(500000), "VNPAY");
        payment.setStatus(PaymentStatus.PENDING);

        Order order = pendingOrder(1L);

        when(paymentGateway.verifyCallback(any())).thenReturn(true);
        when(paymentRecordRepository.findByPaymentId("pay-123")).thenReturn(Optional.of(payment));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(paymentRecordRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "pay-123");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionNo", "GW-TX-001");
        params.put("vnp_SecureHash", "validhash");

        PaymentResultDTO result = paymentAppService.vnpayCallback(params);

        assertTrue(result.success());
        assertEquals(PaymentStatus.SUCCESS, result.paymentStatus());
        assertEquals(OrderStatus.PAID, result.orderStatus());
    }

    @Test
    void vnpayCallback_failure() {
        Payment payment = Payment.init("pay-456", 100L, 1L, BigDecimal.valueOf(500000), "VNPAY");
        payment.setStatus(PaymentStatus.PENDING);

        Order order = pendingOrder(1L);

        when(paymentGateway.verifyCallback(any())).thenReturn(true);
        when(paymentRecordRepository.findByPaymentId("pay-456")).thenReturn(Optional.of(payment));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(paymentRecordRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "pay-456");
        params.put("vnp_ResponseCode", "24"); // User cancelled at VNPAY
        params.put("vnp_TransactionNo", "GW-TX-002");
        params.put("vnp_SecureHash", "validhash");

        PaymentResultDTO result = paymentAppService.vnpayCallback(params);

        assertTrue(result.success()); // Call succeeds, but payment is FAILED
        assertEquals(PaymentStatus.FAILED, result.paymentStatus());
        assertEquals(OrderStatus.PAYMENT_FAILED, result.orderStatus());
    }

    @Test
    void vnpayCallback_invalidSignature() {
        when(paymentGateway.verifyCallback(any())).thenReturn(false);

        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "pay-789");
        params.put("vnp_SecureHash", "invalidhash");

        OrderAppException ex = assertThrows(OrderAppException.class,
                () -> paymentAppService.vnpayCallback(params));
        assertEquals("INVALID_SIGNATURE", ex.code());
    }

    @Test
    void vnpayCallback_paymentNotFound() {
        when(paymentGateway.verifyCallback(any())).thenReturn(true);
        when(paymentRecordRepository.findByPaymentId("nonexistent")).thenReturn(Optional.empty());

        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "nonexistent");
        params.put("vnp_SecureHash", "validhash");

        OrderAppException ex = assertThrows(OrderAppException.class,
                () -> paymentAppService.vnpayCallback(params));
        assertEquals("PAYMENT_NOT_FOUND", ex.code());
    }

    @Test
    void vnpayCallback_alreadyProcessed() {
        Payment payment = Payment.init("pay-done", 100L, 1L, BigDecimal.valueOf(500000), "VNPAY");
        payment.setStatus(PaymentStatus.SUCCESS);

        Order order = pendingOrder(1L);
        order.setStatus(OrderStatus.PAID);

        when(paymentGateway.verifyCallback(any())).thenReturn(true);
        when(paymentRecordRepository.findByPaymentId("pay-done")).thenReturn(Optional.of(payment));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "pay-done");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_SecureHash", "validhash");

        PaymentResultDTO result = paymentAppService.vnpayCallback(params);

        assertTrue(result.success());
        assertEquals(PaymentStatus.SUCCESS, result.paymentStatus());
        // Should not re-save
        verify(paymentRecordRepository, never()).save(any());
    }
}
