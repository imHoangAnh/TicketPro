package com.xxxx.ddd.infrastructure.persistence.mapper;

import com.xxxx.ddd.domain.model.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TicketOrderJPAMapper extends JpaRepository<TicketType, Long> {

    /**
     * Retrieves the available stock for a given ticket type.
     *
     * @param ticketTypeId The ID of the ticket type.
     * @return The available stock quantity.
     */
    @Query("SELECT t.stockAvailable FROM TicketType t WHERE t.id = :ticketTypeId")
    int getStockAvailable(@Param("ticketTypeId") Long ticketTypeId);


    /**
     * Decreases stock if there is enough available stock.
     * Ensures that stock is only deducted when the available quantity is sufficient.
     * Prevents overselling in high-concurrency scenarios (basic)
     *
     * @param ticketTypeId   The ID of the ticket type.
     * @param quantity The quantity to decrease.
     * @return The number of rows affected (should be 0 or 1).
     */
    @Modifying
    @Transactional
    @Query("UPDATE TicketType t SET t.updatedAt = CURRENT_TIMESTAMP, " +
            "t.stockAvailable = t.stockAvailable - :quantity " +
            "WHERE t.id = :ticketTypeId AND t.stockAvailable >= :quantity")
    int decreaseStockLevel1(@Param("ticketTypeId") Long ticketTypeId, @Param("quantity") int quantity);

    @Modifying
    @Transactional
    @Query("UPDATE TicketType t SET t.updatedAt = CURRENT_TIMESTAMP, " +
            "t.stockAvailable = t.stockAvailable - :quantity " +
            "WHERE t.id = :ticketTypeId")
    int decreaseStockLevel0(@Param("ticketTypeId") Long ticketTypeId, @Param("quantity") int quantity);

    @Modifying
    @Transactional
    @Query("UPDATE TicketType t SET t.updatedAt = CURRENT_TIMESTAMP, " +
            "t.stockAvailable = :oldStockAvailable - :quantity " +
            "WHERE t.id = :ticketTypeId AND t.stockAvailable = :oldStockAvailable")
    int decreaseStockLevel3CAS(@Param("ticketTypeId") Long ticketTypeId, @Param("oldStockAvailable") int oldStockAvailable, @Param("quantity") int quantity);

    /**
     *  Hoàn kho và Cập nhật trạng thái trong Database
     */
    @Modifying
    @Transactional
    @Query("UPDATE TicketType t SET t.updatedAt = CURRENT_TIMESTAMP, " +
            "t.stockAvailable = t.stockAvailable + :quantity " +
            "WHERE t.id = :ticketTypeId")
    int increaseStock(@Param("ticketTypeId") Long ticketTypeId, @Param("quantity") int quantity);
}
