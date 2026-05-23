package com.xxxx.ddd.infrastructure.persistence.mapper.ticketing;

import com.xxxx.ddd.domain.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRecordJPAMapper extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentId(String paymentId);

    Optional<Payment> findByOrderId(Long orderId);
}
