package com.xxxx.ddd.application.model;

import lombok.Data;

@Data
public class BookingDTO {
    private Long id;
    private Long ticketTypeId;
    private int quantity;
    private String bookingCode;
    private int status;
}
