package com.darieldon.pedidos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderItemDTO(
        @NotNull
        Long productId,

        @Min(1)
        Integer quantity,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal unitPrice
) {}
