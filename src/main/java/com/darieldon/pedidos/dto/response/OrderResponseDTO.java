package com.darieldon.pedidos.dto.response;

import com.darieldon.pedidos.dto.request.OrderItemDTO;
import com.darieldon.pedidos.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        Long id,
        String customerName,
        List<OrderItemDTO> items,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}