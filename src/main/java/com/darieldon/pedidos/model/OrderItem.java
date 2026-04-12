package com.darieldon.pedidos.model;

import java.math.BigDecimal;

public record OrderItem(
    Long productId,
    Integer quantity,
    BigDecimal unitPrice
) { }
