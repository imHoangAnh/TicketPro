package com.xxxx.ddd.application.model;

import java.time.LocalDateTime;

public record EventSummaryDTO(
        Long id,
        String title,
        String description,
        String venue,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean active
) {
}
