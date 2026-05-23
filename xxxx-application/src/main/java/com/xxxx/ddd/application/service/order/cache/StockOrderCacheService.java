package com.xxxx.ddd.application.service.order.cache;

import com.xxxx.ddd.application.port.cache.KeyValueCachePort;
import com.xxxx.ddd.application.port.cache.StockCacheScriptPort;
import com.xxxx.ddd.domain.model.entity.TicketType;
import com.xxxx.ddd.domain.respository.ticketing.TicketTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class StockOrderCacheService {

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private KeyValueCachePort cache;

    @Autowired
    private StockCacheScriptPort stockCacheScript;

    public boolean addStockAvailableToCache(Long ticketTypeId) {
        if (ticketTypeId == null) {
            return false;
        }

        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId).orElse(null);
        if (ticketType == null) {
            return false;
        }

        String keyStockItemCache = getKeyStockItemCache(ticketTypeId);
        log.info("get->getKeyStockItemCache() | ticketTypeId={}, key={}, stock={}",
                ticketTypeId,
                keyStockItemCache,
                ticketType.getStockAvailable());
        cache.setInt(keyStockItemCache, ticketType.getStockAvailable());
        return true;
    }

    public int decreaseStockCache(Long ticketTypeId, Integer quantity) {
        String keyStockNormal = getKeyStockItemCache(ticketTypeId);
        int stockAvailable = cache.getInt(keyStockNormal);
        log.info("stockAvailable Normal: {}, {}, {} ", keyStockNormal, stockAvailable, stockAvailable - quantity);

        if (stockAvailable >= quantity) {
            cache.setInt(keyStockNormal, stockAvailable - quantity);
            log.info("stockAvailable racing...: {}", stockAvailable - quantity);
            return 1;
        }
        return 0;
    }

    public int decreaseStockCacheByLUA(Long ticketTypeId, Integer quantity) {
        String keyStockLUA = getKeyStockItemCache(ticketTypeId);
        return stockCacheScript.decreaseStock(keyStockLUA, quantity);
    }

    private String getKeyStockItemCache(Long ticketTypeId) {
        return "TICKET_TYPE:" + ticketTypeId + ":STOCK";
    }

    public long getEffectivePrice(Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId).orElse(null);
        if (ticketType == null) {
            return -1L;
        }
        BigDecimal price = ticketType.getPrice();
        return price != null && price.compareTo(BigDecimal.ZERO) > 0 ? price.longValue() : -1L;
    }

    public boolean increaseStockCache(Long ticketTypeId, Integer quantity) {
        String keyStock = getKeyStockItemCache(ticketTypeId);
        return stockCacheScript.increaseStock(keyStock, quantity);
    }
}
