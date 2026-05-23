package com.xxxx.ddd.infrastructure.persistence.repository.ticketing;

import com.xxxx.ddd.domain.model.entity.Payment;
import com.xxxx.ddd.domain.respository.ticketing.PaymentRecordRepository;
import com.xxxx.ddd.infrastructure.persistence.mapper.ticketing.PaymentRecordJPAMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PaymentRecordRepositoryImpl implements PaymentRecordRepository {

    private final PaymentRecordJPAMapper paymentRecordJPAMapper;

    public PaymentRecordRepositoryImpl(PaymentRecordJPAMapper paymentRecordJPAMapper) {
        this.paymentRecordJPAMapper = paymentRecordJPAMapper;
    }

    @Override
    public Payment save(Payment payment) {
        return paymentRecordJPAMapper.save(payment);
    }

    @Override
    public Optional<Payment> findByPaymentId(String paymentId) {
        return paymentRecordJPAMapper.findByPaymentId(paymentId);
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return paymentRecordJPAMapper.findByOrderId(orderId);
    }
}
