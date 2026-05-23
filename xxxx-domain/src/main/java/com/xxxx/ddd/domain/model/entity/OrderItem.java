package com.xxxx.ddd.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "ticket_type_id", nullable = false)
    private Long ticketTypeId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static OrderItem create(Long orderId, Long ticketTypeId, int quantity, BigDecimal unitPrice) {
        BigDecimal totalPrice = unitPrice == null ? null : unitPrice.multiply(BigDecimal.valueOf(quantity));
        return new OrderItem()
                .setOrderId(orderId)
                .setTicketTypeId(ticketTypeId)
                .setQuantity(quantity)
                .setUnitPrice(unitPrice)
                .setTotalPrice(totalPrice);
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
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("orderId is required");
        }
        if (ticketTypeId == null || ticketTypeId <= 0) {
            throw new IllegalArgumentException("ticketTypeId is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("unitPrice must be positive");
        }
        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("totalPrice must be positive");
        }
    }
}

