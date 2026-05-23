package com.xxxx.ddd.application.service.event.impl;

import com.xxxx.ddd.application.model.EventDetailDTO;
import com.xxxx.ddd.application.model.EventSummaryDTO;
import com.xxxx.ddd.application.model.TicketTypeDTO;
import com.xxxx.ddd.application.model.command.CreateEventCommand;
import com.xxxx.ddd.application.model.command.CreateTicketTypeCommand;
import com.xxxx.ddd.application.model.command.UpdateEventCommand;
import com.xxxx.ddd.application.model.command.UpdateTicketTypeCommand;
import com.xxxx.ddd.application.service.event.EventAppException;
import com.xxxx.ddd.application.service.event.EventAppService;
import com.xxxx.ddd.application.service.event.cached.EventCacheInvalidationService;
import com.xxxx.ddd.domain.model.entity.Event;
import com.xxxx.ddd.domain.model.entity.TicketType;
import com.xxxx.ddd.domain.respository.ticketing.EventRepository;
import com.xxxx.ddd.domain.respository.ticketing.TicketTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventAppServiceImpl implements EventAppService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventCacheInvalidationService cacheInvalidationService;

    public EventAppServiceImpl(
            EventRepository eventRepository,
            TicketTypeRepository ticketTypeRepository,
            EventCacheInvalidationService cacheInvalidationService
    ) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.cacheInvalidationService = cacheInvalidationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventSummaryDTO> listActiveEvents() {
        return eventRepository.findActiveEvents().stream()
                .map(EventAppServiceImpl::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventDetailDTO getActiveEventDetail(Long eventId) {
        Event event = findEvent(eventId);
        if (!event.isActive()) {
            throw new EventAppException("Event not found");
        }
        return toDetail(event, true);
    }

    @Override
    @Transactional
    public EventDetailDTO createEvent(CreateEventCommand command) {
        Event event = Event.create(
                command.title(),
                command.description(),
                command.venue(),
                command.startAt(),
                command.endAt(),
                command.active()
        );
        event.validate();
        Event saved = eventRepository.save(event);
        cacheInvalidationService.invalidateEvent(saved.getId());
        return toDetail(saved, false);
    }

    @Override
    @Transactional
    public EventDetailDTO updateEvent(Long eventId, UpdateEventCommand command) {
        Event event = findEvent(eventId)
                .setTitle(command.title())
                .setDescription(command.description())
                .setVenue(command.venue())
                .setStartAt(command.startAt())
                .setEndAt(command.endAt());
        if (command.active() != null) {
            event.setActive(command.active());
        }
        event.validate();
        Event saved = eventRepository.save(event);
        cacheInvalidationService.invalidateEvent(saved.getId());
        return toDetail(saved, false);
    }

    @Override
    @Transactional
    public EventDetailDTO activateEvent(Long eventId) {
        Event event = findEvent(eventId).setActive(true);
        Event saved = eventRepository.save(event);
        cacheInvalidationService.invalidateEvent(saved.getId());
        return toDetail(saved, false);
    }

    @Override
    @Transactional
    public EventDetailDTO inactivateEvent(Long eventId) {
        Event event = findEvent(eventId).setActive(false);
        Event saved = eventRepository.save(event);
        cacheInvalidationService.invalidateEvent(saved.getId());
        return toDetail(saved, false);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = findEvent(eventId).setActive(false);
        Event saved = eventRepository.save(event);
        cacheInvalidationService.invalidateEvent(saved.getId());
    }

    @Override
    @Transactional
    public TicketTypeDTO createTicketType(Long eventId, CreateTicketTypeCommand command) {
        Event event = findEvent(eventId);
        TicketType ticketType = TicketType.create(
                event.getId(),
                command.name(),
                command.description(),
                command.price(),
                command.stockInitial(),
                command.stockInitial()
        );
        ticketType.validate();
        TicketType saved = ticketTypeRepository.save(ticketType);
        cacheInvalidationService.invalidateTicketType(saved.getEventId(), saved.getId());
        return toTicketType(saved);
    }

    @Override
    @Transactional
    public TicketTypeDTO updateTicketType(Long ticketTypeId, UpdateTicketTypeCommand command) {
        TicketType ticketType = findTicketType(ticketTypeId)
                .setName(command.name())
                .setDescription(command.description())
                .setPrice(command.price());
        if (command.stockInitial() != null) {
            ticketType.setStockInitial(command.stockInitial());
        }
        if (command.stockAvailable() != null) {
            ticketType.setStockAvailable(command.stockAvailable());
        }
        if (command.active() != null) {
            ticketType.setActive(command.active());
        }
        ticketType.validate();
        TicketType saved = ticketTypeRepository.save(ticketType);
        cacheInvalidationService.invalidateTicketType(saved.getEventId(), saved.getId());
        return toTicketType(saved);
    }

    @Override
    @Transactional
    public void deleteTicketType(Long ticketTypeId) {
        TicketType ticketType = findTicketType(ticketTypeId).setActive(false);
        TicketType saved = ticketTypeRepository.save(ticketType);
        cacheInvalidationService.invalidateTicketType(saved.getEventId(), saved.getId());
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventAppException("Event not found"));
    }

    private TicketType findTicketType(Long ticketTypeId) {
        return ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new EventAppException("Ticket type not found"));
    }

    private EventDetailDTO toDetail(Event event, boolean activeTicketTypesOnly) {
        List<TicketTypeDTO> ticketTypes = ticketTypeRepository.findByEventId(event.getId()).stream()
                .filter(ticketType -> !activeTicketTypesOnly || ticketType.isActive())
                .map(EventAppServiceImpl::toTicketType)
                .toList();
        return new EventDetailDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getVenue(),
                event.getStartAt(),
                event.getEndAt(),
                event.isActive(),
                ticketTypes
        );
    }

    private static EventSummaryDTO toSummary(Event event) {
        return new EventSummaryDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getVenue(),
                event.getStartAt(),
                event.getEndAt(),
                event.isActive()
        );
    }

    private static TicketTypeDTO toTicketType(TicketType ticketType) {
        return new TicketTypeDTO(
                ticketType.getId(),
                ticketType.getEventId(),
                ticketType.getName(),
                ticketType.getDescription(),
                ticketType.getPrice(),
                ticketType.getStockInitial(),
                ticketType.getStockAvailable(),
                ticketType.isActive()
        );
    }
}
