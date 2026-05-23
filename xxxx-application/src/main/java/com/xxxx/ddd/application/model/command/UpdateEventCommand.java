package com.xxxx.ddd.application.model.command;

import java.time.LocalDateTime;

public record UpdateEventCommand(
        String title,
        String description,
        String venue,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Boolean active
) {
}
