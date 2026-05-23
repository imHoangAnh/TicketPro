package com.xxxx.ddd.application.model.command;

import java.math.BigDecimal;

public record UpdateTicketTypeCommand(
        String name,
        String description,
        BigDecimal price,
        Integer stockInitial,
        Integer stockAvailable,
        Boolean active
) {
}
