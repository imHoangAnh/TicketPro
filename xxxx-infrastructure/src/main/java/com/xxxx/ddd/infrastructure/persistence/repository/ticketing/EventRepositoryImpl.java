package com.xxxx.ddd.infrastructure.persistence.repository.ticketing;

import com.xxxx.ddd.domain.model.entity.Event;
import com.xxxx.ddd.domain.respository.ticketing.EventRepository;
import com.xxxx.ddd.infrastructure.persistence.mapper.ticketing.EventJPAMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EventRepositoryImpl implements EventRepository {

    private final EventJPAMapper eventJPAMapper;

    public EventRepositoryImpl(EventJPAMapper eventJPAMapper) {
        this.eventJPAMapper = eventJPAMapper;
    }

    @Override
    public Event save(Event event) {
        return eventJPAMapper.save(event);
    }

    @Override
    public Optional<Event> findById(Long eventId) {
        return eventJPAMapper.findById(eventId);
    }

    @Override
    public List<Event> findActiveEvents() {
        return eventJPAMapper.findByActiveTrue();
    }

    @Override
    public boolean hasPaidOrders(Long eventId) {
        return eventJPAMapper.countPaidOrdersByEventId(eventId) > 0;
    }
}
