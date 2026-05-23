package com.xxxx.ddd.infrastructure.persistence.repository.ticketing;

import com.xxxx.ddd.domain.model.entity.Order;
import com.xxxx.ddd.domain.respository.ticketing.OrderRepository;
import com.xxxx.ddd.infrastructure.persistence.mapper.ticketing.OrderJPAMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJPAMapper orderJPAMapper;

    public OrderRepositoryImpl(OrderJPAMapper orderJPAMapper) {
        this.orderJPAMapper = orderJPAMapper;
    }

    @Override
    public Order save(Order order) {
        return orderJPAMapper.save(order);
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return orderJPAMapper.findById(orderId);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderJPAMapper.findByOrderNumber(orderNumber);
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderJPAMapper.findByUserId(userId);
    }
}
