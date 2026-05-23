package com.xxxx.ddd.application.mapper;

import com.xxxx.ddd.application.model.BookingDTO;
import com.xxxx.ddd.domain.model.entity.Booking;

public class BookingMapper {

    public static BookingDTO toDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setTicketTypeId(booking.getTicketTypeId());
        dto.setQuantity(booking.getQuantity());
        dto.setBookingCode(booking.getBookingCode());
        dto.setStatus(booking.getStatus());
        return dto;
    }
}
