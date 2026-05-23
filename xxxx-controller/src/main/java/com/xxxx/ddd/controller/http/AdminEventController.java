package com.xxxx.ddd.controller.http;

import com.xxxx.ddd.application.model.EventDetailDTO;
import com.xxxx.ddd.application.model.TicketTypeDTO;
import com.xxxx.ddd.application.model.command.CreateEventCommand;
import com.xxxx.ddd.application.model.command.CreateTicketTypeCommand;
import com.xxxx.ddd.application.model.command.UpdateEventCommand;
import com.xxxx.ddd.application.model.command.UpdateTicketTypeCommand;
import com.xxxx.ddd.application.service.event.EventAppException;
import com.xxxx.ddd.application.service.event.EventAppService;
import com.xxxx.ddd.controller.dto.CreateEventRequest;
import com.xxxx.ddd.controller.dto.CreateTicketTypeRequest;
import com.xxxx.ddd.controller.dto.UpdateEventRequest;
import com.xxxx.ddd.controller.dto.UpdateTicketTypeRequest;
import com.xxxx.ddd.controller.model.enums.ResultUtil;
import com.xxxx.ddd.controller.model.vo.ResultMessage;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminEventController {

    private final EventAppService eventAppService;

    public AdminEventController(EventAppService eventAppService) {
        this.eventAppService = eventAppService;
    }

    @PostMapping("/events")
    public ResponseEntity<ResultMessage<EventDetailDTO>> createEvent(@Valid @RequestBody CreateEventRequest request) {
        try {
            EventDetailDTO event = eventAppService.createEvent(new CreateEventCommand(
                    request.getTitle(),
                    request.getDescription(),
                    request.getVenue(),
                    request.getStartAt(),
                    request.getEndAt(),
                    request.isActive()
            ));
            return ResponseEntity.status(HttpStatus.CREATED).body(ResultUtil.data(event));
        } catch (EventAppException | IllegalArgumentException e) {
            return badRequest(e);
        }
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<ResultMessage<EventDetailDTO>> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventRequest request
    ) {
        try {
            return ResponseEntity.ok(ResultUtil.data(eventAppService.updateEvent(eventId, new UpdateEventCommand(
                    request.getTitle(),
                    request.getDescription(),
                    request.getVenue(),
                    request.getStartAt(),
                    request.getEndAt(),
                    request.getActive()
            ))));
        } catch (EventAppException e) {
            return notFound(e);
        } catch (IllegalArgumentException e) {
            return badRequest(e);
        }
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<ResultMessage<Boolean>> deleteEvent(@PathVariable Long eventId) {
        try {
            eventAppService.deleteEvent(eventId);
            return ResponseEntity.ok(ResultUtil.data(true));
        } catch (EventAppException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResultUtil.error(404, e.getMessage()));
        }
    }

    @PutMapping("/events/{eventId}/active")
    public ResponseEntity<ResultMessage<EventDetailDTO>> activateEvent(@PathVariable Long eventId) {
        try {
            return ResponseEntity.ok(ResultUtil.data(eventAppService.activateEvent(eventId)));
        } catch (EventAppException e) {
            return notFound(e);
        }
    }

    @PutMapping("/events/{eventId}/inactive")
    public ResponseEntity<ResultMessage<EventDetailDTO>> inactivateEvent(@PathVariable Long eventId) {
        try {
            return ResponseEntity.ok(ResultUtil.data(eventAppService.inactivateEvent(eventId)));
        } catch (EventAppException e) {
            return notFound(e);
        }
    }

    @PostMapping("/events/{eventId}/ticket-types")
    public ResponseEntity<ResultMessage<TicketTypeDTO>> createTicketType(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateTicketTypeRequest request
    ) {
        try {
            TicketTypeDTO ticketType = eventAppService.createTicketType(eventId, new CreateTicketTypeCommand(
                    request.getName(),
                    request.getDescription(),
                    request.getPrice(),
                    request.getStockInitial()
            ));
            return ResponseEntity.status(HttpStatus.CREATED).body(ResultUtil.data(ticketType));
        } catch (EventAppException e) {
            return notFound(e);
        } catch (IllegalArgumentException e) {
            return badRequest(e);
        }
    }

    @PutMapping("/ticket-types/{ticketTypeId}")
    public ResponseEntity<ResultMessage<TicketTypeDTO>> updateTicketType(
            @PathVariable Long ticketTypeId,
            @Valid @RequestBody UpdateTicketTypeRequest request
    ) {
        try {
            return ResponseEntity.ok(ResultUtil.data(eventAppService.updateTicketType(ticketTypeId, new UpdateTicketTypeCommand(
                    request.getName(),
                    request.getDescription(),
                    request.getPrice(),
                    request.getStockInitial(),
                    request.getStockAvailable(),
                    request.getActive()
            ))));
        } catch (EventAppException e) {
            return notFound(e);
        } catch (IllegalArgumentException e) {
            return badRequest(e);
        }
    }

    @DeleteMapping("/ticket-types/{ticketTypeId}")
    public ResponseEntity<ResultMessage<Boolean>> deleteTicketType(@PathVariable Long ticketTypeId) {
        try {
            eventAppService.deleteTicketType(ticketTypeId);
            return ResponseEntity.ok(ResultUtil.data(true));
        } catch (EventAppException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResultUtil.error(404, e.getMessage()));
        }
    }

    private static <T> ResponseEntity<ResultMessage<T>> badRequest(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultUtil.error(400, e.getMessage()));
    }

    private static <T> ResponseEntity<ResultMessage<T>> notFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResultUtil.error(404, e.getMessage()));
    }
}
