package com.darieldon.pedidos.dto.response;

import com.darieldon.pedidos.dto.request.OrderItemDTO;
import com.darieldon.pedidos.model.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        Long id,
        String customerName,
        List<OrderItemDTO> items,
        OrderStatus status,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
        BigDecimal totalAmount, // Força que no Json o valor fique com 2 casas decimais.
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}