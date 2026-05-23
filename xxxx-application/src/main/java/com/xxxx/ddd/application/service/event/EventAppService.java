package com.xxxx.ddd.application.service.event;

import com.xxxx.ddd.application.model.EventDetailDTO;
import com.xxxx.ddd.application.model.EventSummaryDTO;
import com.xxxx.ddd.application.model.TicketTypeDTO;
import com.xxxx.ddd.application.model.command.CreateEventCommand;
import com.xxxx.ddd.application.model.command.CreateTicketTypeCommand;
import com.xxxx.ddd.application.model.command.UpdateEventCommand;
import com.xxxx.ddd.application.model.command.UpdateTicketTypeCommand;

import java.util.List;

public interface EventAppService {

    List<EventSummaryDTO> listActiveEvents();

    EventDetailDTO getActiveEventDetail(Long eventId);

    EventDetailDTO createEvent(CreateEventCommand command);

    EventDetailDTO updateEvent(Long eventId, UpdateEventCommand command);

    EventDetailDTO activateEvent(Long eventId);

    EventDetailDTO inactivateEvent(Long eventId);

    void deleteEvent(Long eventId);

    TicketTypeDTO createTicketType(Long eventId, CreateTicketTypeCommand command);

    TicketTypeDTO updateTicketType(Long ticketTypeId, UpdateTicketTypeCommand command);

    void deleteTicketType(Long ticketTypeId);
}
