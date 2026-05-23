package com.xxxx.ddd.application.model;

import com.xxxx.ddd.domain.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
        Long id,
        String orderNumber,
        Long userId,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemDTO> items
) {
}
