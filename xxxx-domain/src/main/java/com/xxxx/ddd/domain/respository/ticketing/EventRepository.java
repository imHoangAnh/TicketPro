package com.xxxx.ddd.domain.respository.ticketing;

import com.xxxx.ddd.domain.model.entity.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository {
    Event save(Event event);

    Optional<Event> findById(Long eventId);

    List<Event> findActiveEvents();

    boolean hasPaidOrders(Long eventId);
}
