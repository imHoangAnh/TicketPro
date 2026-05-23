package com.xxxx.ddd.domain.respository;

import com.xxxx.ddd.domain.model.entity.Booking;

import java.util.Optional;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findByBookingCode(String bookingCode);
}