package com.xxxx.ddd.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateEventRequest {

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String venue;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    private boolean active = true;
}
