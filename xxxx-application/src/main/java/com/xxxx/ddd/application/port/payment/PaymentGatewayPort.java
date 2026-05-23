package com.xxxx.ddd.application.port.payment;

import com.xxxx.ddd.domain.model.entity.Payment;

import java.util.Map;

public interface PaymentGatewayPort {

    String createPaymentUrl(Payment payment);

    boolean verifyCallback(Map<String, String> callbackParams);
}
