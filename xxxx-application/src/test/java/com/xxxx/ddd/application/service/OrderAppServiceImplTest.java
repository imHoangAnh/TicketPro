package com.xxxx.ddd.application.service;

import com.xxxx.ddd.application.model.command.CreateOrderCommand;
import com.xxxx.ddd.application.service.order.OrderAppException;
import com.xxxx.ddd.application.service.order.cache.StockOrderCacheService;
import com.xxxx.ddd.application.service.order.impl.OrderAppServiceImpl;
import com.xxxx.ddd.domain.model.entity.Event;
import com.xxxx.ddd.domain.model.entity.Order;
import com.xxxx.ddd.domain.model.entity.OrderItem;
import com.xxxx.ddd.domain.model.entity.TicketType;
import com.xxxx.ddd.domain.model.enums.OrderStatus;
import com.xxxx.ddd.domain.respository.ticketing.EventRepository;
import com.xxxx.ddd.domain.respository.ticketing.OrderItemRepository;
import com.xxxx.ddd.domain.respository.ticketing.OrderRepository;
import com.xxxx.ddd.domain.respository.ticketing.TicketTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrderAppServiceImplTest {

    private InMemoryOrderRepository orders;
    private InMemoryOrderItemRepository orderItems;
    private InMemoryTicketTypeRepository ticketTypes;
    private InMemoryEventRepository events;
    private StockOrderCacheService stockCache;
    private OrderAppServiceImpl service;

    @BeforeEach
    void setUp() {
        orders = new InMemoryOrderRepository();
        orderItems = new InMemoryOrderItemRepository();
        ticketTypes = new InMemoryTicketTypeRepository();
        events = new InMemoryEventRepository();
        stockCache = mock(StockOrderCacheService.class);
        service = new OrderAppServiceImpl(orders, orderItems, ticketTypes, events, stockCache);
    }

    @Test
    void placeOrderUsesRedisGateAndCreatesOrderWithConditionalMysqlStockUpdate() {
        TicketType ticketType = activeTicketType();
        when(stockCache.decreaseStockCacheByLUA(ticketType.getId(), 2)).thenReturn(1);

        var response = service.placeOrder(10L, new CreateOrderCommand(ticketType.getId(), 2));

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getOrderId()).isNotNull();
        assertThat(ticketTypes.findById(ticketType.getId()).orElseThrow().getStockAvailable()).isEqualTo(3);
        assertThat(orderItems.findByOrderId(response.getOrderId())).hasSize(1);
    }

    @Test
    void placeOrderWarmsRedisStockOnCacheMissAndRetriesOnce() {
        TicketType ticketType = activeTicketType();
        when(stockCache.decreaseStockCacheByLUA(ticketType.getId(), 1)).thenReturn(-1, 1);
        when(stockCache.addStockAvailableToCache(ticketType.getId())).thenReturn(true);

        var response = service.placeOrder(10L, new CreateOrderCommand(ticketType.getId(), 1));

        assertThat(response.isSuccess()).isTrue();
        verify(stockCache).addStockAvailableToCache(ticketType.getId());
    }

    @Test
    void placeOrderFailsWhenRedisStockIsStillMissingAfterWarmup() {
        TicketType ticketType = activeTicketType();
        when(stockCache.decreaseStockCacheByLUA(ticketType.getId(), 1)).thenReturn(-1, -1);
        when(stockCache.addStockAvailableToCache(ticketType.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.placeOrder(10L, new CreateOrderCommand(ticketType.getId(), 1)))
                .isInstanceOf(OrderAppException.class)
                .hasMessageContaining("Could not warm ticket stock");
        assertThat(orders.findByUserId(10L)).isEmpty();
    }

    @Test
    void placeOrderStopsWhenRedisReportsOutOfStock() {
        TicketType ticketType = activeTicketType();
        when(stockCache.decreaseStockCacheByLUA(ticketType.getId(), 6)).thenReturn(0);

        var response = service.placeOrder(10L, new CreateOrderCommand(ticketType.getId(), 6));

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo("OUT_OF_STOCK");
        assertThat(orders.findByUserId(10L)).isEmpty();
        assertThat(ticketTypes.findById(ticketType.getId()).orElseThrow().getStockAvailable()).isEqualTo(5);
    }

    @Test
    void placeOrderRejectsInactiveEventBeforeRedisReservation() {
        TicketType ticketType = activeTicketType();
        events.findById(ticketType.getEventId()).orElseThrow().setActive(false);

        assertThatThrownBy(() -> service.placeOrder(10L, new CreateOrderCommand(ticketType.getId(), 1)))
                .isInstanceOf(OrderAppException.class)
                .hasMessageContaining("Event is not active");
        verifyNoInteractions(stockCache);
    }

    @Test
    void placeOrderRollsBackRedisWhenMysqlStockUpdateFails() {
        TicketType ticketType = activeTicketType();
        ticketType.setStockAvailable(0);
        when(stockCache.decreaseStockCacheByLUA(ticketType.getId(), 1)).thenReturn(1);
        when(stockCache.increaseStockCache(ticketType.getId(), 1)).thenReturn(true);

        var response = service.placeOrder(10L, new CreateOrderCommand(ticketType.getId(), 1));

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo("STOCK_CONFLICT");
        verify(stockCache).increaseStockCache(ticketType.getId(), 1);
    }

    @Test
    void cancelOwnPendingOrderRestoresMysqlAndRedisStock() {
        TicketType ticketType = activeTicketType();
        Order order = orders.save(Order.createPending(10L, "ORD-10-1", BigDecimal.valueOf(20)).setStatus(OrderStatus.PENDING));
        orderItems.save(OrderItem.create(order.getId(), ticketType.getId(), 2, BigDecimal.TEN));
        ticketType.setStockAvailable(3);
        when(stockCache.increaseStockCache(ticketType.getId(), 2)).thenReturn(true);

        var cancelled = service.cancelOrder(10L, false, order.getId());

        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(ticketTypes.findById(ticketType.getId()).orElseThrow().getStockAvailable()).isEqualTo(5);
        verify(stockCache).increaseStockCache(ticketType.getId(), 2);
    }

    @Test
    void userCannotCancelAnotherUsersOrderButAdminCan() {
        TicketType ticketType = activeTicketType();
        Order order = orders.save(Order.createPending(20L, "ORD-20-1", BigDecimal.TEN).setStatus(OrderStatus.PENDING));
        orderItems.save(OrderItem.create(order.getId(), ticketType.getId(), 1, BigDecimal.TEN));
        when(stockCache.increaseStockCache(ticketType.getId(), 1)).thenReturn(true);

        assertThatThrownBy(() -> service.cancelOrder(10L, false, order.getId()))
                .isInstanceOf(OrderAppException.class)
                .hasMessageContaining("Order does not belong to user");

        var cancelled = service.cancelOrder(99L, true, order.getId());

        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    private TicketType activeTicketType() {
        Event event = events.save(event(true));
        return ticketTypes.save(TicketType.create(event.getId(), "Standard", "Seat", BigDecimal.TEN, 5, 5));
    }

    private static Event event(boolean active) {
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        return Event.create("Concert", "Description", "Venue", startAt, startAt.plusHours(2), active);
    }

    private static final class InMemoryOrderRepository implements OrderRepository {
        private final Map<Long, Order> byId = new HashMap<>();
        private long nextId = 1;

        @Override
        public Order save(Order order) {
            order.validate();
            if (order.getId() == null) {
                order.setId(nextId++);
            }
            LocalDateTime now = LocalDateTime.now();
            if (order.getCreatedAt() == null) {
                order.setCreatedAt(now);
            }
            order.setUpdatedAt(now);
            byId.put(order.getId(), order);
            return order;
        }

        @Override
        public Optional<Order> findById(Long orderId) {
            return Optional.ofNullable(byId.get(orderId));
        }

        @Override
        public Optional<Order> findByOrderNumber(String orderNumber) {
            return byId.values().stream()
                    .filter(order -> order.getOrderNumber().equals(orderNumber))
                    .findFirst();
        }

        @Override
        public List<Order> findByUserId(Long userId) {
            return byId.values().stream()
                    .filter(order -> order.getUserId().equals(userId))
                    .toList();
        }
    }

    private static final class InMemoryOrderItemRepository implements OrderItemRepository {
        private final List<OrderItem> items = new ArrayList<>();
        private long nextId = 1;

        @Override
        public OrderItem save(OrderItem orderItem) {
            orderItem.validate();
            if (orderItem.getId() == null) {
                orderItem.setId(nextId++);
            }
            LocalDateTime now = LocalDateTime.now();
            if (orderItem.getCreatedAt() == null) {
                orderItem.setCreatedAt(now);
            }
            orderItem.setUpdatedAt(now);
            items.add(orderItem);
            return orderItem;
        }

        @Override
        public List<OrderItem> findByOrderId(Long orderId) {
            return items.stream()
                    .filter(item -> item.getOrderId().equals(orderId))
                    .toList();
        }
    }

    private static final class InMemoryTicketTypeRepository implements TicketTypeRepository {
        private final Map<Long, TicketType> byId = new HashMap<>();
        private long nextId = 1;

        @Override
        public TicketType save(TicketType ticketType) {
            ticketType.validate();
            if (ticketType.getId() == null) {
                ticketType.setId(nextId++);
            }
            byId.put(ticketType.getId(), ticketType);
            return ticketType;
        }

        @Override
        public Optional<TicketType> findById(Long ticketTypeId) {
            return Optional.ofNullable(byId.get(ticketTypeId));
        }

        @Override
        public List<TicketType> findByEventId(Long eventId) {
            return byId.values().stream()
                    .filter(ticketType -> ticketType.getEventId().equals(eventId))
                    .toList();
        }

        @Override
        public boolean decreaseStockIfAvailable(Long ticketTypeId, int quantity) {
            TicketType ticketType = byId.get(ticketTypeId);
            if (ticketType == null || ticketType.getStockAvailable() < quantity) {
                return false;
            }
            ticketType.setStockAvailable(ticketType.getStockAvailable() - quantity);
            return true;
        }

        @Override
        public boolean increaseStock(Long ticketTypeId, int quantity) {
            TicketType ticketType = byId.get(ticketTypeId);
            if (ticketType == null) {
                return false;
            }
            ticketType.setStockAvailable(ticketType.getStockAvailable() + quantity);
            return true;
        }
    }

    private static final class InMemoryEventRepository implements EventRepository {
        private final Map<Long, Event> byId = new HashMap<>();
        private long nextId = 1;

        @Override
        public Event save(Event event) {
            event.validate();
            if (event.getId() == null) {
                event.setId(nextId++);
            }
            byId.put(event.getId(), event);
            return event;
        }

        @Override
        public Optional<Event> findById(Long eventId) {
            return Optional.ofNullable(byId.get(eventId));
        }

        @Override
        public List<Event> findActiveEvents() {
            return byId.values().stream().filter(Event::isActive).toList();
        }

        @Override
        public boolean hasPaidOrders(Long eventId) {
            return false;
        }
    }
}
