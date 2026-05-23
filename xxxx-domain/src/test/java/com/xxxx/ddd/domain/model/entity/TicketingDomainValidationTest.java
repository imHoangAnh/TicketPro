package com.xxxx.ddd.domain.model.entity;

import com.xxxx.ddd.domain.model.enums.RoleName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketingDomainValidationTest {

    @Test
    void ticketTypeRequiresPositivePriceAndNonNegativeStock() {
        assertThatThrownBy(() -> TicketType.create(1L, "VIP", null, BigDecimal.ZERO, 10, 10).validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("price");

        assertThatThrownBy(() -> TicketType.create(1L, "VIP", null, BigDecimal.TEN, 10, -1).validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock");

        TicketType ticketType = TicketType.create(1L, "VIP", null, BigDecimal.TEN, 10, 5);

        ticketType.validate();

        assertThat(ticketType.getEventId()).isEqualTo(1L);
        assertThat(ticketType.getStockAvailable()).isEqualTo(5);
    }

    @Test
    void orderItemUsesTicketTypeIdAndPositiveQuantity() {
        assertThatThrownBy(() -> OrderItem.create(1L, null, 1, BigDecimal.TEN).validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ticketTypeId");

        assertThatThrownBy(() -> OrderItem.create(1L, 2L, 0, BigDecimal.TEN).validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity");

        OrderItem orderItem = OrderItem.create(1L, 2L, 3, BigDecimal.valueOf(100));

        orderItem.validate();

        assertThat(orderItem.getTicketTypeId()).isEqualTo(2L);
        assertThat(orderItem.getTotalPrice()).isEqualByComparingTo("300");
    }

    @Test
    void eventRequiresValidTimeRange() {
        LocalDateTime startAt = LocalDateTime.parse("2026-06-20T18:00:00");

        assertThatThrownBy(() -> Event.create("Concert", null, "HCMC", startAt, startAt.minusHours(1), true).validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("endAt");
    }

    @Test
    void roleNameIsValidated() {
        assertThatThrownBy(() -> Role.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("role name");

        Role role = Role.of(RoleName.ADMIN);

        assertThat(role.getName()).isEqualTo(RoleName.ADMIN);
    }
}
