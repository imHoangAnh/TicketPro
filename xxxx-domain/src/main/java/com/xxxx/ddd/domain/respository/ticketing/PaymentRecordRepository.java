package com.xxxx.ddd.domain.respository.ticketing;

import com.xxxx.ddd.domain.model.entity.Payment;

import java.util.Optional;

public interface PaymentRecordRepository {
    Payment save(Payment payment);

    Optional<Payment> findByPaymentId(String paymentId);

    Optional<Payment> findByOrderId(Long orderId);
}
