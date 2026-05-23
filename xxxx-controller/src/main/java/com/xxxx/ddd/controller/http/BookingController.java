package com.xxxx.ddd.controller.http;

import com.xxxx.ddd.application.model.BookingDTO;
import com.xxxx.ddd.application.model.command.CreateBookingCommand;
import com.xxxx.ddd.application.service.booking.BookingAppService;
import com.xxxx.ddd.controller.dto.CreateBookingRequest;
import com.xxxx.ddd.controller.mapper.BookingControllerMapper;
import com.xxxx.ddd.controller.model.enums.ResultUtil;
import com.xxxx.ddd.controller.model.vo.ResultMessage;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@Slf4j
public class BookingController {

    @Autowired
    private BookingAppService bookingAppService;

    @PostMapping
    public ResultMessage<BookingDTO> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        log.info("Creating booking for ticketTypeId: {}, quantity: {}", request.getTicketTypeId(), request.getQuantity());
        try {
            CreateBookingCommand command = BookingControllerMapper.toCommand(request);
            BookingDTO dto = bookingAppService.createBooking(command);
            return ResultUtil.data(dto);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResultUtil.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("Error creating booking", e);
            return ResultUtil.error(500, "Failed to create booking");
        }
    }
}
