package com.xxxx.ddd.application.model.command;

import lombok.Data;

@Data
public class CreateBookingCommand {
    private Long ticketTypeId;
    private int quantity;
}
