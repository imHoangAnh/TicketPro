package com.xxxx.ddd.domain.service;

import com.xxxx.ddd.domain.model.entity.Booking;

public interface BookingDomainService {
    Booking createBooking(Long ticketTypeId, int quantity);
}
