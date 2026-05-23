package com.xxxx.ddd.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateTicketTypeRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal price;

    @Min(0)
    private Integer stockInitial;

    @Min(0)
    private Integer stockAvailable;

    private Boolean active;
}
