package com.xxxx.ddd.application.service;

import com.xxxx.ddd.application.port.cache.KeyValueCachePort;
import com.xxxx.ddd.application.model.command.CreateEventCommand;
import com.xxxx.ddd.application.model.command.CreateTicketTypeCommand;
import com.xxxx.ddd.application.model.command.UpdateTicketTypeCommand;
import com.xxxx.ddd.application.service.event.EventAppException;
import com.xxxx.ddd.application.service.event.cached.EventCacheInvalidationService;
import com.xxxx.ddd.application.service.event.impl.EventAppServiceImpl;
import com.xxxx.ddd.domain.model.entity.Event;
import com.xxxx.ddd.domain.model.entity.TicketType;
import com.xxxx.ddd.domain.respository.ticketing.EventRepository;
import com.xxxx.ddd.domain.respository.ticketing.TicketTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EventAppServiceImplTest {

    private InMemoryEventRepository events;
    private InMemoryTicketTypeRepository ticketTypes;
    private KeyValueCachePort cache;
    private EventAppServiceImpl service;

    @BeforeEach
    void setUp() {
        events = new InMemoryEventRepository();
        ticketTypes = new InMemoryTicketTypeRepository();
        cache = mock(KeyValueCachePort.class);
        service = new EventAppServiceImpl(events, ticketTypes, new EventCacheInvalidationService(cache));
    }

    @Test
    void listActiveEventsReturnsOnlyActiveEvents() {
        events.save(event("Active", true));
        events.save(event("Inactive", false));

        var activeEvents = service.listActiveEvents();

        assertThat(activeEvents).extracting("title").containsExactly("Active");
    }

    @Test
    void publicDetailRequiresActiveEventAndFiltersInactiveTicketTypes() {
        Event event = events.save(event("Concert", true));
        ticketTypes.save(ticket(event.getId(), "Standard", true));
        ticketTypes.save(ticket(event.getId(), "Hidden", false));

        var detail = service.getActiveEventDetail(event.getId());

        assertThat(detail.ticketTypes()).extracting("name").containsExactly("Standard");

        event.setActive(false);
        assertThatThrownBy(() -> service.getActiveEventDetail(event.getId()))
                .isInstanceOf(EventAppException.class)
                .hasMessageContaining("Event not found");
    }

    @Test
    void createEventValidatesAndInvalidatesActiveList() {
        var created = service.createEvent(new CreateEventCommand(
                "Festival",
                "Outdoor event",
                "Main Hall",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                true
        ));

        assertThat(created.id()).isNotNull();
        verify(cache).delete(EventCacheInvalidationService.ACTIVE_EVENT_LIST_KEY);
        verify(cache).delete(EventCacheInvalidationService.EVENT_KEY_PREFIX + created.id());
    }

    @Test
    void createTicketTypeValidatesStockAndInvalidatesEventAndTicketTypeKeys() {
        Event event = events.save(event("Concert", true));

        var created = service.createTicketType(event.getId(), new CreateTicketTypeCommand(
                "VIP",
                "Front section",
                BigDecimal.valueOf(100),
                20
        ));

        assertThat(created.stockInitial()).isEqualTo(20);
        assertThat(created.stockAvailable()).isEqualTo(20);
        verify(cache).delete(EventCacheInvalidationService.EVENT_KEY_PREFIX + event.getId());
        verify(cache).delete(EventCacheInvalidationService.TICKET_TYPE_KEY_PREFIX + created.id());
    }

    @Test
    void updateTicketTypeRejectsAvailableStockAboveInitialStock() {
        Event event = events.save(event("Concert", true));
        TicketType ticketType = ticketTypes.save(ticket(event.getId(), "Standard", true));

        assertThatThrownBy(() -> service.updateTicketType(ticketType.getId(), new UpdateTicketTypeCommand(
                "Standard",
                "Updated",
                BigDecimal.valueOf(10),
                5,
                6,
                true
        ))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("available stock cannot exceed initial stock");
    }

    @Test
    void deleteEventAndTicketTypeInactivateRecords() {
        Event event = events.save(event("Concert", true));
        TicketType ticketType = ticketTypes.save(ticket(event.getId(), "Standard", true));

        service.deleteEvent(event.getId());
        service.deleteTicketType(ticketType.getId());

        assertThat(events.findById(event.getId()).orElseThrow().isActive()).isFalse();
        assertThat(ticketTypes.findById(ticketType.getId()).orElseThrow().isActive()).isFalse();
    }

    private static Event event(String title, boolean active) {
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        return Event.create(title, "Description", "Venue", startAt, startAt.plusHours(2), active);
    }

    private static TicketType ticket(Long eventId, String name, boolean active) {
        return TicketType.create(eventId, name, "Description", BigDecimal.valueOf(25), 100, 100)
                .setActive(active);
    }

    private static final class InMemoryEventRepository implements EventRepository {
        private final Map<Long, Event> byId = new HashMap<>();
        private long nextId = 1;

        @Override
        public Event save(Event event) {
            event.validate();
            if (event.getId() == null) {
                event.setId(nextId++);
            }
            byId.put(event.getId(), event);
            return event;
        }

        @Override
        public Optional<Event> findById(Long eventId) {
            return Optional.ofNullable(byId.get(eventId));
        }

        @Override
        public List<Event> findActiveEvents() {
            return byId.values().stream().filter(Event::isActive).toList();
        }

        @Override
        public boolean hasPaidOrders(Long eventId) {
            return false;
        }
    }

    private static final class InMemoryTicketTypeRepository implements TicketTypeRepository {
        private final Map<Long, TicketType> byId = new HashMap<>();
        private long nextId = 1;

        @Override
        public TicketType save(TicketType ticketType) {
            ticketType.validate();
            if (ticketType.getId() == null) {
                ticketType.setId(nextId++);
            }
            byId.put(ticketType.getId(), ticketType);
            return ticketType;
        }

        @Override
        public Optional<TicketType> findById(Long ticketTypeId) {
            return Optional.ofNullable(byId.get(ticketTypeId));
        }

        @Override
        public List<TicketType> findByEventId(Long eventId) {
            return byId.values().stream()
                    .filter(ticketType -> ticketType.getEventId().equals(eventId))
                    .toList();
        }

        @Override
        public boolean decreaseStockIfAvailable(Long ticketTypeId, int quantity) {
            return false;
        }

        @Override
        public boolean increaseStock(Long ticketTypeId, int quantity) {
            return false;
        }
    }
}
