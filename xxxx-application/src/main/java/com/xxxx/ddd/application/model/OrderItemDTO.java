package com.xxxx.ddd.application.model;

import java.math.BigDecimal;

public record OrderItemDTO(
        Long id,
        Long ticketTypeId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}
