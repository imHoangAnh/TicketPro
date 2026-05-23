package com.xxxx.ddd.application.model.command;

import java.math.BigDecimal;

public record CreateTicketTypeCommand(
        String name,
        String description,
        BigDecimal price,
        int stockInitial
) {
}
