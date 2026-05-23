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
@Table(name = "ticket_types")
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_initial", nullable = false)
    private int stockInitial;

    @Column(name = "stock_available", nullable = false)
    private int stockAvailable;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static TicketType create(Long eventId, String name, String description, BigDecimal price, int stockInitial, int stockAvailable) {
        return new TicketType()
                .setEventId(eventId)
                .setName(name)
                .setDescription(description)
                .setPrice(price)
                .setStockInitial(stockInitial)
                .setStockAvailable(stockAvailable)
                .setActive(true);
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
        if (eventId == null || eventId <= 0) {
            throw new IllegalArgumentException("eventId is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("ticket type name is required");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("ticket type price must be positive");
        }
        if (stockInitial < 0 || stockAvailable < 0) {
            throw new IllegalArgumentException("ticket type stock must be non-negative");
        }
        if (stockAvailable > stockInitial) {
            throw new IllegalArgumentException("available stock cannot exceed initial stock");
        }
    }
}
