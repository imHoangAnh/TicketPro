package com.xxxx.ddd.domain.service.impl;

import com.xxxx.ddd.domain.model.entity.TickerOrder;
import com.xxxx.ddd.domain.respository.OrderDeductionRepository;
import com.xxxx.ddd.domain.service.OrderDeductionDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderDeductionDomainServiceImpl implements OrderDeductionDomainService {

    @Autowired
    private OrderDeductionRepository orderDeductionRepository;
    @Override
    public void insertOrder(String yearMonth, TickerOrder tickerOrder) {
        orderDeductionRepository.insertOrder(yearMonth, tickerOrder);
    }

    @Override
    public List<Object[]> findAll(String yearMonth) {
        return orderDeductionRepository.findAll(yearMonth);//List.of();
    }

    @Override
    public Object[] findByOrderNumber(String yearMonth, String orderNumber) {
        return orderDeductionRepository.findByOrderNumber(yearMonth, orderNumber);//new Object[0];
    }

    @Override
    public List<Object[]> findByDateRange(String yearMonth, LocalDateTime startDate, LocalDateTime endDate) {
        return List.of();
    }

    @Override
    public boolean updateOrderStatus(String yearMonth, String orderNumber, Integer status) {
        return orderDeductionRepository.updateOrderStatus(yearMonth, orderNumber, status);
    }

    @Override
    public List<Object[]> findPage(String yearMonth, long lastId, int limit) {
        return orderDeductionRepository.findPage(yearMonth, lastId, limit);
    }
}
