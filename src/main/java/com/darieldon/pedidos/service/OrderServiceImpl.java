package com.darieldon.pedidos.service;

import com.darieldon.pedidos.dto.request.OrderRequestDTO;
import com.darieldon.pedidos.dto.request.UpdateStatusDTO;
import com.darieldon.pedidos.dto.response.OrderCreatedDTO;
import com.darieldon.pedidos.dto.response.OrderResponseDTO;
import com.darieldon.pedidos.exception.BadRequestException;
import com.darieldon.pedidos.exception.ResourceNotFoundException;
import com.darieldon.pedidos.mapper.OrderMapper;
import com.darieldon.pedidos.model.Order;
import com.darieldon.pedidos.model.OrderStatus;
import com.darieldon.pedidos.repository.OrderRepository;
import com.darieldon.pedidos.strategy.DiscountStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final Map<String, DiscountStrategy> discountStrategies;

    @Override
    public OrderCreatedDTO create(OrderRequestDTO dto, Long userId){
        BigDecimal subtotal = dto.items().stream()
                .map(item -> item.unitPrice()
                        .multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String clientType = dto.clientType().name();
        DiscountStrategy strategy = discountStrategies.get(clientType);
        if (strategy == null) {
            throw new BadRequestException("Tipo de cliente inválido para desconto: " + clientType);
        }

        BigDecimal total = strategy.calculate(subtotal)
                .setScale(2, RoundingMode.HALF_UP);

        Order order = mapper.toEntity(dto, userId);
        order.setTotalAmount(total);

        Order saved = repository.save(order);
        return new OrderCreatedDTO(saved.getId(), "Pedido criado com sucesso.");
    }

    @Override
    public Page<OrderResponseDTO> findAll(Pageable pageable, OrderStatus status) {
        Page<Order> page = status != null ? repository.findAllByStatusAndDeletedAtIsNull(status, pageable)
                : repository.findAllByDeletedAtIsNull(pageable);

        return mapper.toDTOPage(page);
    }

    @Override
    public OrderResponseDTO findById(Long id){
        Order order = findOrThrow(id);
        return mapper.toDTO(order);
    }

    @Override
    public void updateStatus(Long id, UpdateStatusDTO dto){
        Order order = findOrThrow(id);
        order.setStatus(dto.status());
        repository.save(order);
    }

    @Override
    public void deleteById(Long id){
        Order order = findOrThrow(id);
        order.setDeletedAt(LocalDateTime.now());
        repository.save(order);
    }

    private Order findOrThrow(Long id){
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido não encontrado" + id));
    }
}
