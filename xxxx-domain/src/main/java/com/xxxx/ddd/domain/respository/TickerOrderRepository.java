package com.xxxx.ddd.domain.respository;

public interface TickerOrderRepository {
    boolean decreaseStockLevel1(Long ticketTypeId, int quantity);
    boolean decreaseStockLevel2(Long ticketTypeId, int quantity);
    boolean decreaseStockLevel3CAS(Long ticketTypeId, int oldStockAvailable, int quantity);

    int getStockAvailable(Long ticketTypeId);

    /**
     * Thực hiện câu lệnh SQL hoàn kho vào Database
     */
    boolean increaseStock(Long ticketTypeId, int quantity);
}
