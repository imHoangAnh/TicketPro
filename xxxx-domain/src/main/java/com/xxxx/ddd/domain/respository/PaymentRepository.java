package com.xxxx.ddd.domain.respository;

import com.xxxx.ddd.domain.model.entity.PaymentTransaction;

public interface PaymentRepository {
    boolean save(PaymentTransaction transaction);
    boolean updateStatus(String paymentId, Integer status, String gatewayId, String url);
    PaymentTransaction findByPaymentId(String paymentId);
}
