package com.darieldon.pedidos.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemDTO(
        @NotNull
        Long productId,

        @Min(1)
        Integer quantity
) {}
