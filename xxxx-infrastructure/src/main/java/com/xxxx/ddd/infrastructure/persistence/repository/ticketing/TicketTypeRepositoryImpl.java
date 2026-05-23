package com.xxxx.ddd.infrastructure.persistence.repository.ticketing;

import com.xxxx.ddd.domain.model.entity.TicketType;
import com.xxxx.ddd.domain.respository.ticketing.TicketTypeRepository;
import com.xxxx.ddd.infrastructure.persistence.mapper.ticketing.TicketTypeJPAMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TicketTypeRepositoryImpl implements TicketTypeRepository {

    private final TicketTypeJPAMapper ticketTypeJPAMapper;

    public TicketTypeRepositoryImpl(TicketTypeJPAMapper ticketTypeJPAMapper) {
        this.ticketTypeJPAMapper = ticketTypeJPAMapper;
    }

    @Override
    public TicketType save(TicketType ticketType) {
        return ticketTypeJPAMapper.save(ticketType);
    }

    @Override
    public Optional<TicketType> findById(Long ticketTypeId) {
        return ticketTypeJPAMapper.findById(ticketTypeId);
    }

    @Override
    public List<TicketType> findByEventId(Long eventId) {
        return ticketTypeJPAMapper.findByEventId(eventId);
    }

    @Override
    public boolean decreaseStockIfAvailable(Long ticketTypeId, int quantity) {
        return ticketTypeJPAMapper.decreaseStockIfAvailable(ticketTypeId, quantity) > 0;
    }

    @Override
    public boolean increaseStock(Long ticketTypeId, int quantity) {
        return ticketTypeJPAMapper.increaseStock(ticketTypeId, quantity) > 0;
    }
}
