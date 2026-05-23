package com.xxxx.ddd.controller.dto;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class InventorBucketRequest {

    @NotBlank(message = "skuId cannot be blank")
    private String skuId;

    @NotBlank(message = "sellerId cannot be blank")
    private String sellerId;

    @NotNull(message = "inventoryNum cannot be null")
    @Min(value = 1, message = "inventoryNum must be greater than 0")
    private Integer inventoryNum;

    private String inventorCode;

    private Long templateId;
}