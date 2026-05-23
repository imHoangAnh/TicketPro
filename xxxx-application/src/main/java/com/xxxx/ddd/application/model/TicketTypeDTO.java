package com.xxxx.ddd.application.model;

import java.math.BigDecimal;

public record TicketTypeDTO(
        Long id,
        Long eventId,
        String name,
        String description,
        BigDecimal price,
        int stockInitial,
        int stockAvailable,
        boolean active
) {
}
