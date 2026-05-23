package com.xxxx.ddd.infrastructure.persistence.mapper.ticketing;

import com.xxxx.ddd.domain.model.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemJPAMapper extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
}
