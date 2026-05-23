package com.xxxx.ddd.infrastructure.persistence.mapper.ticketing;

import com.xxxx.ddd.domain.model.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventJPAMapper extends JpaRepository<Event, Long> {
    List<Event> findByActiveTrue();

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM orders o
                    JOIN order_items oi ON oi.order_id = o.id
                    JOIN ticket_types tt ON tt.id = oi.ticket_type_id
                    WHERE tt.event_id = :eventId
                      AND o.status = 'PAID'
                    """,
            nativeQuery = true
    )
    long countPaidOrdersByEventId(@Param("eventId") Long eventId);
}
