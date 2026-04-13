package com.darieldon.pedidos.repository;

import com.darieldon.pedidos.model.Order;
import com.darieldon.pedidos.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndDeletedAtIsNull(Long id);

    Page<Order> findAllByDeletedAtIsNull(Pageable pageable);

    Page<Order> findAllByStatusAndDeletedAtIsNull(OrderStatus status, Pageable pageable);
}
