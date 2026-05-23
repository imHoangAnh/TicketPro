package com.xxxx.ddd.domain.model.entity;

import com.xxxx.ddd.domain.model.enums.OrderStatus;
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
@Entity(name = "TicketingOrder")
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 64)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Order createPending(Long userId, String orderNumber, BigDecimal totalAmount) {
        return new Order()
                .setUserId(userId)
                .setOrderNumber(orderNumber)
                .setTotalAmount(totalAmount)
                .setStatus(OrderStatus.PENDING);
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
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId is required");
        }
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException("orderNumber is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("order status is required");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("totalAmount must be non-negative");
        }
    }
}

