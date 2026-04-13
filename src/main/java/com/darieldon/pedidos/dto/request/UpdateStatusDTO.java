package com.darieldon.pedidos.dto.request;

import com.darieldon.pedidos.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusDTO(
        @NotNull
        OrderStatus status
) {}
