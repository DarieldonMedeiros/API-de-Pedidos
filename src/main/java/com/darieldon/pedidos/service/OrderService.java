package com.darieldon.pedidos.service;

import com.darieldon.pedidos.dto.request.OrderRequestDTO;
import com.darieldon.pedidos.dto.request.UpdateStatusDTO;
import com.darieldon.pedidos.dto.response.OrderCreatedDTO;
import com.darieldon.pedidos.dto.response.OrderResponseDTO;
import com.darieldon.pedidos.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderCreatedDTO create(OrderRequestDTO dto, Long userId);
    Page<OrderResponseDTO> findAll(Pageable pageable, OrderStatus status);
    OrderResponseDTO findById(Long id);
    void updateStatus(Long id, UpdateStatusDTO status);
    void deleteById(Long id);
}
