package com.xxxx.ddd.domain.service.impl;

import com.xxxx.ddd.domain.model.entity.Booking;
import com.xxxx.ddd.domain.respository.BookingRepository;
import com.xxxx.ddd.domain.service.BookingDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class BookingDomainServiceImpl implements BookingDomainService {

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public Booking createBooking(Long ticketTypeId, int quantity) {
        if (ticketTypeId == null || ticketTypeId <= 0) {
            throw new IllegalArgumentException("Invalid ticket type ID");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (quantity > 10) {
            throw new IllegalArgumentException("Cannot book more than 10 tickets at once");
        }

        Booking booking = new Booking();
        booking.setTicketTypeId(ticketTypeId);
        booking.setQuantity(quantity);
        booking.setBookingCode(generateBookingCode());
        booking.setStatus(1); // CONFIRMED
        booking.setCreatedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);
        log.info("Created booking: {} for ticketTypeId: {}", saved.getBookingCode(), ticketTypeId);
        return saved;
    }

    private String generateBookingCode() {
        return "BK" + System.currentTimeMillis()
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
