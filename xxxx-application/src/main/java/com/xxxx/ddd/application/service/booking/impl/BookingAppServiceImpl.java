package com.xxxx.ddd.application.service.booking.impl;

import com.xxxx.ddd.application.mapper.BookingMapper;
import com.xxxx.ddd.application.model.BookingDTO;
import com.xxxx.ddd.application.model.command.CreateBookingCommand;
import com.xxxx.ddd.application.service.booking.BookingAppService;
import com.xxxx.ddd.domain.model.entity.Booking;
import com.xxxx.ddd.domain.service.BookingDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BookingAppServiceImpl implements BookingAppService {

    @Autowired
    private BookingDomainService bookingDomainService;

    @Override
    public BookingDTO createBooking(CreateBookingCommand command) {
        Booking booking = bookingDomainService.createBooking(command.getTicketTypeId(), command.getQuantity());
        log.info("App Service: booking created with code: {}", booking.getBookingCode());
        return BookingMapper.toDTO(booking);
    }
}
