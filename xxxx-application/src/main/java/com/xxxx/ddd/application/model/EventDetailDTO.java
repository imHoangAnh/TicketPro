package com.xxxx.ddd.application.model;

import java.time.LocalDateTime;
import java.util.List;

public record EventDetailDTO(
        Long id,
        String title,
        String description,
        String venue,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean active,
        List<TicketTypeDTO> ticketTypes
) {
}
