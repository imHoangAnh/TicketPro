package com.xxxx.ddd.application.model.command;

import java.time.LocalDateTime;

public record CreateEventCommand(
        String title,
        String description,
        String venue,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean active
) {
}
