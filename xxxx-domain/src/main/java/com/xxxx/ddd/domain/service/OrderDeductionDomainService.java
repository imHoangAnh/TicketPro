package com.xxxx.ddd.domain.service;

import com.xxxx.ddd.domain.model.entity.TickerOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderDeductionDomainService {
    void insertOrder(String yearMonth, TickerOrder tickerOrder);
    List<Object[]> findAll(String yearMonth);
    Object[] findByOrderNumber(String yearMonth, String orderNumber);
    List<Object[]> findByDateRange(String yearMonth, LocalDateTime startDate, LocalDateTime endDate);

    // update status
    boolean updateOrderStatus(String yearMonth, String orderNumber, Integer status);

    List<Object[]> findPage(String yearMonth, long lastId, int limit);
}
