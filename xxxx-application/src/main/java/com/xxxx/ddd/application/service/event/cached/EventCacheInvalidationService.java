package com.xxxx.ddd.application.service.event.cached;

import com.xxxx.ddd.application.port.cache.KeyValueCachePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventCacheInvalidationService {

    public static final String ACTIVE_EVENT_LIST_KEY = "EVENT:LIST:ACTIVE";
    public static final String EVENT_KEY_PREFIX = "EVENT:";
    public static final String TICKET_TYPE_KEY_PREFIX = "TICKET_TYPE:";

    private final KeyValueCachePort cache;

    public EventCacheInvalidationService(KeyValueCachePort cache) {
        this.cache = cache;
    }

    public void invalidateEvent(Long eventId) {
        deleteQuietly(ACTIVE_EVENT_LIST_KEY);
        if (eventId != null) {
            deleteQuietly(EVENT_KEY_PREFIX + eventId);
        }
    }

    public void invalidateTicketType(Long eventId, Long ticketTypeId) {
        invalidateEvent(eventId);
        if (ticketTypeId != null) {
            deleteQuietly(TICKET_TYPE_KEY_PREFIX + ticketTypeId);
        }
    }

    private void deleteQuietly(String key) {
        try {
            cache.delete(key);
        } catch (RuntimeException e) {
            log.warn("Failed to invalidate event cache key={}", key, e);
        }
    }
}
