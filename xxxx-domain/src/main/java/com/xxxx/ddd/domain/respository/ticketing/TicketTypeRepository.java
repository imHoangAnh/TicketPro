package com.xxxx.ddd.domain.respository.ticketing;

import com.xxxx.ddd.domain.model.entity.TicketType;

import java.util.List;
import java.util.Optional;

public interface TicketTypeRepository {
    TicketType save(TicketType ticketType);

    Optional<TicketType> findById(Long ticketTypeId);

    List<TicketType> findByEventId(Long eventId);

    boolean decreaseStockIfAvailable(Long ticketTypeId, int quantity);

    boolean increaseStock(Long ticketTypeId, int quantity);
}
