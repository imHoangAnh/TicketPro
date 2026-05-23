package com.xxxx.ddd.domain.service;

public interface TickerOrderDomainService {

    boolean decreaseStockLevel1(Long ticketTypeId, int quantity);

    boolean decreaseStockLevel2(Long ticketTypeId, int quantity);

    boolean decreaseStockLevel3CAS(Long ticketTypeId, int oldStockAvailable, int quantity);

    int getStockAvailable(Long ticketTypeId);

    boolean increaseStock(Long ticketTypeId, int quantity);
}
