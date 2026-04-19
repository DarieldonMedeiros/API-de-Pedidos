package com.darieldon.pedidos.model;

import java.io.Serializable;
import java.math.BigDecimal;

public record OrderItem(
    Long productId,
    Integer quantity,
    BigDecimal unitPrice
) implements Serializable { }
