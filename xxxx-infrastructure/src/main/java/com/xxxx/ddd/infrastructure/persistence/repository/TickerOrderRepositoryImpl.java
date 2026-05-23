package com.xxxx.ddd.infrastructure.persistence.repository;

import com.xxxx.ddd.domain.respository.TickerOrderRepository;
import com.xxxx.ddd.infrastructure.persistence.mapper.TicketOrderJPAMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TickerOrderRepositoryImpl implements TickerOrderRepository {

    @Autowired
    private TicketOrderJPAMapper ticketOrderJPAMapper;

    @Override
    public boolean decreaseStockLevel1(Long ticketTypeId, int quantity) {
        log.info("Run test:decreaseStockLevel1 with: | {}, {} ", ticketTypeId, quantity);
        return ticketOrderJPAMapper.decreaseStockLevel1(ticketTypeId, quantity) > 0;
    }

    @Override
    public boolean decreaseStockLevel2(Long ticketTypeId, int quantity) {
        return false;
    }

    @Override
    public boolean decreaseStockLevel3CAS(Long ticketTypeId, int oldStockAvailable, int quantity) {
        log.info("Run test:decreaseStockLevel3CAS with: | {}, {}, {} ", ticketTypeId, oldStockAvailable, quantity);
        return ticketOrderJPAMapper.decreaseStockLevel3CAS(ticketTypeId, oldStockAvailable, quantity) > 0;
    }

    @Override
    public int getStockAvailable(Long ticketTypeId) {
        return ticketOrderJPAMapper.getStockAvailable(ticketTypeId);
    }

    @Override
    public boolean increaseStock(Long ticketTypeId, int quantity) {
        log.info("Rollback stock: increaseStock for ticketTypeId: {} | quantity: {}", ticketTypeId, quantity);
        return ticketOrderJPAMapper.increaseStock(ticketTypeId, quantity) > 0;
    }
}
