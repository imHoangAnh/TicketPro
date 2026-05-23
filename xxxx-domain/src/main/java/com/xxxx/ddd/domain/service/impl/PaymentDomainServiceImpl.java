package com.xxxx.ddd.domain.service.impl;

import com.xxxx.ddd.domain.model.entity.PaymentTransaction;
import com.xxxx.ddd.domain.respository.PaymentRepository;
import com.xxxx.ddd.domain.service.PaymentDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentDomainServiceImpl implements PaymentDomainService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public boolean createTransaction(PaymentTransaction transaction) {
        log.info("Domain: Creating payment transaction for order: {}", transaction.getOrderNumber());
        // Thiết lập các logic nghiệp vụ mặc định nếu có
        if (transaction.getPaymentStatus() == null) {
            transaction.setPaymentStatus(0); // Mặc định là INIT
        }
        return paymentRepository.save(transaction);
    }

    @Override
    public boolean updateTransactionInProgress(String paymentId, String paymentUrl) {
        log.info("Domain: Updating transaction {} to IN_PROGRESS with URL", paymentId);
        // Trạng thái 1 là IN_PROGRESS
        return paymentRepository.updateStatus(paymentId, 1, null, paymentUrl);
    }
}
