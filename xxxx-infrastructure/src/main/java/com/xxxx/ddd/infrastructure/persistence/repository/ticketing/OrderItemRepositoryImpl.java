package com.xxxx.ddd.infrastructure.persistence.repository.ticketing;

import com.xxxx.ddd.domain.model.entity.OrderItem;
import com.xxxx.ddd.domain.respository.ticketing.OrderItemRepository;
import com.xxxx.ddd.infrastructure.persistence.mapper.ticketing.OrderItemJPAMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderItemRepositoryImpl implements OrderItemRepository {

    private final OrderItemJPAMapper orderItemJPAMapper;

    public OrderItemRepositoryImpl(OrderItemJPAMapper orderItemJPAMapper) {
        this.orderItemJPAMapper = orderItemJPAMapper;
    }

    @Override
    public OrderItem save(OrderItem orderItem) {
        return orderItemJPAMapper.save(orderItem);
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return orderItemJPAMapper.findByOrderId(orderId);
    }
}
