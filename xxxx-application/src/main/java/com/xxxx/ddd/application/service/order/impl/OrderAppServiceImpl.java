package com.xxxx.ddd.application.service.order.impl;

import com.xxxx.ddd.application.model.OrderDTO;
import com.xxxx.ddd.application.model.OrderItemDTO;
import com.xxxx.ddd.application.model.command.CreateOrderCommand;
import com.xxxx.ddd.application.model.response.PlaceOrderResponse;
import com.xxxx.ddd.application.service.order.OrderAppException;
import com.xxxx.ddd.application.service.order.OrderAppService;
import com.xxxx.ddd.application.service.order.cache.StockOrderCacheService;
import com.xxxx.ddd.domain.model.entity.Event;
import com.xxxx.ddd.domain.model.entity.Order;
import com.xxxx.ddd.domain.model.entity.OrderItem;
import com.xxxx.ddd.domain.model.entity.TicketType;
import com.xxxx.ddd.domain.model.enums.OrderStatus;
import com.xxxx.ddd.domain.respository.ticketing.EventRepository;
import com.xxxx.ddd.domain.respository.ticketing.OrderItemRepository;
import com.xxxx.ddd.domain.respository.ticketing.OrderRepository;
import com.xxxx.ddd.domain.respository.ticketing.TicketTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class OrderAppServiceImpl implements OrderAppService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;
    private final StockOrderCacheService stockOrderCacheService;

    public OrderAppServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            TicketTypeRepository ticketTypeRepository,
            EventRepository eventRepository,
            StockOrderCacheService stockOrderCacheService
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.eventRepository = eventRepository;
        this.stockOrderCacheService = stockOrderCacheService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlaceOrderResponse placeOrder(Long userId, CreateOrderCommand command) {
        validateUser(userId);
        validateCommand(command);

        TicketType ticketType = loadBookableTicketType(command.ticketTypeId());
        int redisResult = reserveRedisStock(ticketType.getId(), command.quantity());
        if (redisResult == 0) {
            log.info("placeOrder: out of stock userId={} ticketTypeId={}", userId, ticketType.getId());
            return PlaceOrderResponse.failed("OUT_OF_STOCK", "Ticket stock is not enough");
        }

        boolean redisReserved = true;
        try {
            boolean dbStockUpdated = ticketTypeRepository.decreaseStockIfAvailable(ticketType.getId(), command.quantity());
            if (!dbStockUpdated) {
                rollbackRedisReservation(ticketType.getId(), command.quantity());
                redisReserved = false;
                log.warn("placeOrder: MySQL stock conflict userId={} ticketTypeId={}", userId, ticketType.getId());
                return PlaceOrderResponse.failed("STOCK_CONFLICT", "Order could not reserve stock");
            }

            BigDecimal totalAmount = ticketType.getPrice().multiply(BigDecimal.valueOf(command.quantity()));
            Order order = orderRepository.save(Order.createPending(userId, newOrderNumber(userId), totalAmount));
            orderItemRepository.save(OrderItem.create(order.getId(), ticketType.getId(), command.quantity(), ticketType.getPrice()));

            log.info("placeOrder: success userId={} orderId={} ticketTypeId={}", userId, order.getId(), ticketType.getId());
            return PlaceOrderResponse.success(order.getId());
        } catch (RuntimeException e) {
            if (redisReserved) {
                rollbackRedisReservation(ticketType.getId(), command.quantity());
            }
            throw e;
        }
    }

    @Override
    public List<OrderDTO> listMyOrders(Long userId) {
        validateUser(userId);
        return orderRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public OrderDTO getOrder(Long actorUserId, boolean admin, Long orderId) {
        validateUser(actorUserId);
        Order order = loadOrder(orderId);
        assertCanAccess(actorUserId, admin, order);
        return toDto(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO cancelOrder(Long actorUserId, boolean admin, Long orderId) {
        validateUser(actorUserId);
        Order order = loadOrder(orderId);
        assertCanAccess(actorUserId, admin, order);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderAppException("ORDER_NOT_CANCELLABLE", "Only pending orders can be cancelled");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        if (items.isEmpty()) {
            throw new OrderAppException("ORDER_ITEM_NOT_FOUND", "Order item not found");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        for (OrderItem item : items) {
            boolean dbRestored = ticketTypeRepository.increaseStock(item.getTicketTypeId(), item.getQuantity());
            if (!dbRestored) {
                throw new OrderAppException("STOCK_RESTORE_FAILED", "Could not restore ticket stock");
            }
            boolean redisRestored = stockOrderCacheService.increaseStockCache(item.getTicketTypeId(), item.getQuantity());
            if (!redisRestored) {
                log.warn("cancelOrder: Redis stock restore failed orderId={} ticketTypeId={}", order.getId(), item.getTicketTypeId());
                throw new OrderAppException("REDIS_STOCK_RESTORE_FAILED", "Could not restore Redis stock");
            }
        }

        log.info("cancelOrder: success actorUserId={} admin={} orderId={}", actorUserId, admin, order.getId());
        return toDto(order);
    }

    private int reserveRedisStock(Long ticketTypeId, int quantity) {
        int redisResult = stockOrderCacheService.decreaseStockCacheByLUA(ticketTypeId, quantity);
        if (redisResult == -1) {
            log.info("reserveRedisStock: cache miss ticketTypeId={}, warming stock", ticketTypeId);
            boolean warmed = stockOrderCacheService.addStockAvailableToCache(ticketTypeId);
            if (!warmed) {
                throw new OrderAppException("STOCK_CACHE_WARM_FAILED", "Could not warm ticket stock");
            }
            redisResult = stockOrderCacheService.decreaseStockCacheByLUA(ticketTypeId, quantity);
        }
        if (redisResult == -1) {
            throw new OrderAppException("STOCK_CACHE_WARM_FAILED", "Could not warm ticket stock");
        }
        return redisResult;
    }

    private void rollbackRedisReservation(Long ticketTypeId, int quantity) {
        boolean restored = stockOrderCacheService.increaseStockCache(ticketTypeId, quantity);
        if (!restored) {
            log.error("rollbackRedisReservation: Redis rollback failed ticketTypeId={} quantity={}", ticketTypeId, quantity);
        }
    }

    private TicketType loadBookableTicketType(Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new OrderAppException("TICKET_TYPE_NOT_FOUND", "Ticket type not found"));
        if (!ticketType.isActive()) {
            throw new OrderAppException("TICKET_TYPE_INACTIVE", "Ticket type is not active");
        }

        Event event = eventRepository.findById(ticketType.getEventId())
                .orElseThrow(() -> new OrderAppException("EVENT_NOT_FOUND", "Event not found"));
        if (!event.isActive()) {
            throw new OrderAppException("EVENT_INACTIVE", "Event is not active");
        }
        return ticketType;
    }

    private Order loadOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new OrderAppException("ORDER_NOT_FOUND", "Order not found");
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderAppException("ORDER_NOT_FOUND", "Order not found"));
    }

    private void assertCanAccess(Long actorUserId, boolean admin, Order order) {
        if (!admin && !order.getUserId().equals(actorUserId)) {
            throw new OrderAppException("ORDER_FORBIDDEN", "Order does not belong to user");
        }
    }

    private OrderDTO toDto(Order order) {
        List<OrderItemDTO> items = orderItemRepository.findByOrderId(order.getId()).stream()
                .map(item -> new OrderItemDTO(
                        item.getId(),
                        item.getTicketTypeId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .toList();
        return new OrderDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items
        );
    }

    private static void validateUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new OrderAppException("USER_NOT_FOUND", "Authenticated user is required");
        }
    }

    private static void validateCommand(CreateOrderCommand command) {
        if (command == null || command.ticketTypeId() == null || command.ticketTypeId() <= 0) {
            throw new OrderAppException("TICKET_TYPE_NOT_FOUND", "Ticket type not found");
        }
        if (command.quantity() <= 0) {
            throw new OrderAppException("INVALID_QUANTITY", "Quantity must be positive");
        }
    }

    private static String newOrderNumber(Long userId) {
        return "ORD-" + userId + "-" + System.currentTimeMillis();
    }
}
