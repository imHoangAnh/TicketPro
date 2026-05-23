package com.xxxx.ddd.domain.model.entity;

import com.xxxx.ddd.domain.model.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, unique = true, length = 64)
    private String paymentId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "method", nullable = false, length = 32)
    private String method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PaymentStatus status;

    @Column(name = "gateway_transaction_id", length = 120)
    private String gatewayTransactionId;

    @Column(name = "payment_url")
    private String paymentUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Payment init(String paymentId, Long orderId, Long userId, BigDecimal amount, String method) {
        return new Payment()
                .setPaymentId(paymentId)
                .setOrderId(orderId)
                .setUserId(userId)
                .setAmount(amount)
                .setMethod(method)
                .setStatus(PaymentStatus.INIT);
    }

    @PrePersist
    void onCreate() {
        validate();
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        validate();
        updatedAt = LocalDateTime.now();
    }

    public void validate() {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId is required");
        }
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("orderId is required");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("payment method is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("payment status is required");
        }
    }
}
