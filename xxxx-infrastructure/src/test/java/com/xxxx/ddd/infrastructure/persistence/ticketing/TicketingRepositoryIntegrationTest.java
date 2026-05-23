package com.xxxx.ddd.infrastructure.persistence.ticketing;

import com.xxxx.ddd.domain.model.entity.Event;
import com.xxxx.ddd.domain.model.entity.Order;
import com.xxxx.ddd.domain.model.entity.OrderItem;
import com.xxxx.ddd.domain.model.entity.Payment;
import com.xxxx.ddd.domain.model.entity.Role;
import com.xxxx.ddd.domain.model.entity.TicketType;
import com.xxxx.ddd.domain.model.entity.User;
import com.xxxx.ddd.domain.model.entity.UserRole;
import com.xxxx.ddd.domain.model.enums.OrderStatus;
import com.xxxx.ddd.domain.model.enums.RoleName;
import com.xxxx.ddd.domain.respository.ticketing.EventRepository;
import com.xxxx.ddd.domain.respository.ticketing.OrderItemRepository;
import com.xxxx.ddd.domain.respository.ticketing.OrderRepository;
import com.xxxx.ddd.domain.respository.ticketing.PaymentRecordRepository;
import com.xxxx.ddd.domain.respository.ticketing.RoleRepository;
import com.xxxx.ddd.domain.respository.ticketing.TicketTypeRepository;
import com.xxxx.ddd.domain.respository.ticketing.UserRepository;
import com.xxxx.ddd.domain.respository.ticketing.UserRoleRepository;
import com.xxxx.ddd.infrastructure.persistence.mapper.ticketing.UserJPAMapper;
import com.xxxx.ddd.infrastructure.persistence.repository.ticketing.EventRepositoryImpl;
import com.xxxx.ddd.infrastructure.persistence.repository.ticketing.OrderItemRepositoryImpl;
import com.xxxx.ddd.infrastructure.persistence.repository.ticketing.OrderRepositoryImpl;
import com.xxxx.ddd.infrastructure.persistence.repository.ticketing.PaymentRecordRepositoryImpl;
import com.xxxx.ddd.infrastructure.persistence.repository.ticketing.RoleRepositoryImpl;
import com.xxxx.ddd.infrastructure.persistence.repository.ticketing.TicketTypeRepositoryImpl;
import com.xxxx.ddd.infrastructure.persistence.repository.ticketing.UserRepositoryImpl;
import com.xxxx.ddd.infrastructure.persistence.repository.ticketing.UserRoleRepositoryImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@Import({
        UserRepositoryImpl.class,
        RoleRepositoryImpl.class,
        UserRoleRepositoryImpl.class,
        EventRepositoryImpl.class,
        TicketTypeRepositoryImpl.class,
        OrderRepositoryImpl.class,
        OrderItemRepositoryImpl.class,
        PaymentRecordRepositoryImpl.class
})
class TicketingRepositoryIntegrationTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = User.class)
    @EnableJpaRepositories(basePackageClasses = UserJPAMapper.class)
    static class TestConfig {
    }

    @jakarta.annotation.Resource
    private UserRepository userRepository;

    @jakarta.annotation.Resource
    private RoleRepository roleRepository;

    @jakarta.annotation.Resource
    private UserRoleRepository userRoleRepository;

    @jakarta.annotation.Resource
    private EventRepository eventRepository;

    @jakarta.annotation.Resource
    private TicketTypeRepository ticketTypeRepository;

    @jakarta.annotation.Resource
    private OrderRepository orderRepository;

    @jakarta.annotation.Resource
    private OrderItemRepository orderItemRepository;

    @jakarta.annotation.Resource
    private PaymentRecordRepository paymentRecordRepository;

    @jakarta.annotation.Resource
    private EntityManager entityManager;

    @Test
    void repositoriesPersistTicketingModelAndProtectStock() {
        Role userRole = roleRepository.save(Role.of(RoleName.USER));
        User user = userRepository.save(User.registerLocal("buyer@example.com", "{noop}password", "Buyer"));
        userRoleRepository.save(UserRole.assign(user.getId(), userRole.getId()));

        Event event = eventRepository.save(Event.create(
                "Spring Music Festival",
                "Sample active event",
                "Ho Chi Minh City",
                LocalDateTime.parse("2026-06-20T18:00:00"),
                LocalDateTime.parse("2026-06-20T23:00:00"),
                true
        ));
        TicketType ticketType = ticketTypeRepository.save(TicketType.create(
                event.getId(),
                "VIP",
                "VIP access",
                BigDecimal.valueOf(750000),
                10,
                10
        ));

        assertThat(eventRepository.findActiveEvents()).extracting(Event::getId).contains(event.getId());
        assertThat(ticketTypeRepository.findByEventId(event.getId())).hasSize(1);

        assertThat(ticketTypeRepository.decreaseStockIfAvailable(ticketType.getId(), 4)).isTrue();
        entityManager.flush();
        entityManager.clear();

        TicketType afterDecrement = ticketTypeRepository.findById(ticketType.getId()).orElseThrow();
        assertThat(afterDecrement.getStockAvailable()).isEqualTo(6);
        assertThat(ticketTypeRepository.decreaseStockIfAvailable(ticketType.getId(), 7)).isFalse();

        Order order = orderRepository.save(Order.createPending(user.getId(), "ORD-TEST-001", BigDecimal.valueOf(750000)));
        orderItemRepository.save(OrderItem.create(order.getId(), ticketType.getId(), 1, BigDecimal.valueOf(750000)));
        paymentRecordRepository.save(Payment.init("PAY-TEST-001", order.getId(), user.getId(), BigDecimal.valueOf(750000), "MOCK"));

        entityManager.flush();
        entityManager.clear();

        assertThat(eventRepository.hasPaidOrders(event.getId())).isFalse();

        Order savedOrder = orderRepository.findByOrderNumber("ORD-TEST-001").orElseThrow();
        savedOrder.setStatus(OrderStatus.PAID);
        orderRepository.save(savedOrder);
        entityManager.flush();
        entityManager.clear();

        assertThat(eventRepository.hasPaidOrders(event.getId())).isTrue();
        assertThat(orderItemRepository.findByOrderId(savedOrder.getId())).hasSize(1);
        assertThat(paymentRecordRepository.findByPaymentId("PAY-TEST-001")).isPresent();
    }

    @Test
    void uniqueUserEmailIsEnforced() {
        userRepository.save(User.registerLocal("duplicate@example.com", "{noop}password", "First"));
        entityManager.flush();

        assertThatThrownBy(() -> userRepository.save(User.registerLocal("duplicate@example.com", "{noop}password", "Second")))
                .isInstanceOf(RuntimeException.class);
    }
}
