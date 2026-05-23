package com.xxxx.ddd.infrastructure.persistence.mapper.ticketing;

import com.xxxx.ddd.domain.model.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TicketTypeJPAMapper extends JpaRepository<TicketType, Long> {
    List<TicketType> findByEventId(Long eventId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE TicketType t
               SET t.stockAvailable = t.stockAvailable - :quantity,
                   t.updatedAt = CURRENT_TIMESTAMP
             WHERE t.id = :ticketTypeId
               AND t.stockAvailable >= :quantity
            """)
    int decreaseStockIfAvailable(@Param("ticketTypeId") Long ticketTypeId, @Param("quantity") int quantity);

    @Modifying
    @Transactional
    @Query("""
            UPDATE TicketType t
               SET t.stockAvailable = t.stockAvailable + :quantity,
                   t.updatedAt = CURRENT_TIMESTAMP
             WHERE t.id = :ticketTypeId
            """)
    int increaseStock(@Param("ticketTypeId") Long ticketTypeId, @Param("quantity") int quantity);
}
