package com.xxxx.ddd.application.service.booking;

import com.xxxx.ddd.application.model.BookingDTO;
import com.xxxx.ddd.application.model.command.CreateBookingCommand;

public interface BookingAppService {
    BookingDTO createBooking(CreateBookingCommand command);
}