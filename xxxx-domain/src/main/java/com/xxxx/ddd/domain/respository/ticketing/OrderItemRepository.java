package com.xxxx.ddd.domain.respository.ticketing;

import com.xxxx.ddd.domain.model.entity.OrderItem;

import java.util.List;

public interface OrderItemRepository {
    OrderItem save(OrderItem orderItem);

    List<OrderItem> findByOrderId(Long orderId);
}
