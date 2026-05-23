package com.xxxx.ddd.domain.service.impl;

import com.xxxx.ddd.domain.respository.TickerOrderRepository;
import com.xxxx.ddd.domain.service.TickerOrderDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TickerOrderDomainServiceImpl implements TickerOrderDomainService {

    @Autowired
    private TickerOrderRepository tickerOrderRepository;

    @Override
    public boolean decreaseStockLevel1(Long ticketTypeId, int quantity) {
        return tickerOrderRepository.decreaseStockLevel1(ticketTypeId, quantity);
    }

    @Override
    public boolean decreaseStockLevel2(Long ticketTypeId, int quantity) {
        return false;
    }

    @Override
    public boolean decreaseStockLevel3CAS(Long ticketTypeId, int oldStockAvailable, int quantity) {
        return tickerOrderRepository.decreaseStockLevel3CAS(ticketTypeId, oldStockAvailable, quantity);
    }

    @Override
    public int getStockAvailable(Long ticketTypeId) {
        return tickerOrderRepository.getStockAvailable(ticketTypeId);
    }

    @Override
    public boolean increaseStock(Long ticketTypeId, int quantity) {
        return tickerOrderRepository.increaseStock(ticketTypeId, quantity);
    }
}
