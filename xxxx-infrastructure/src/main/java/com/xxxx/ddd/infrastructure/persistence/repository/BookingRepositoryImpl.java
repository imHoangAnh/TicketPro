package com.xxxx.ddd.infrastructure.persistence.repository;

import com.xxxx.ddd.domain.model.entity.Booking;
import com.xxxx.ddd.domain.respository.BookingRepository;
import com.xxxx.ddd.infrastructure.persistence.mapper.BookingJPAMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class BookingRepositoryImpl implements BookingRepository {

    @Autowired
    private BookingJPAMapper bookingJPAMapper;

    @Override
    public Booking save(Booking booking) {
        log.info("Saving booking for ticketTypeId: {}", booking.getTicketTypeId());
        return bookingJPAMapper.save(booking);
    }

    @Override
    public Optional<Booking> findByBookingCode(String bookingCode) {
        return bookingJPAMapper.findByBookingCode(bookingCode);
    }
}
