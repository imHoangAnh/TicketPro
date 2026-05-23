package com.xxxx.ddd.application.service.order;

import com.xxxx.ddd.application.model.OrderDTO;
import com.xxxx.ddd.application.model.command.CreateOrderCommand;
import com.xxxx.ddd.application.model.response.PlaceOrderResponse;

import java.util.List;

public interface OrderAppService {

    PlaceOrderResponse placeOrder(Long userId, CreateOrderCommand command);

    List<OrderDTO> listMyOrders(Long userId);

    OrderDTO getOrder(Long actorUserId, boolean admin, Long orderId);

    OrderDTO cancelOrder(Long actorUserId, boolean admin, Long orderId);
}
