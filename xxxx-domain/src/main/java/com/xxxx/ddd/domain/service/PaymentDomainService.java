package com.xxxx.ddd.domain.service;

import com.xxxx.ddd.domain.model.entity.PaymentTransaction;

public interface PaymentDomainService {
    boolean createTransaction(PaymentTransaction transaction);
    boolean updateTransactionInProgress(String paymentId, String paymentUrl);
}
