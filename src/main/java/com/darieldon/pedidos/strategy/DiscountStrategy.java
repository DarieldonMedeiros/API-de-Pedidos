package com.darieldon.pedidos.strategy;

import java.math.BigDecimal;

public interface DiscountStrategy {

    BigDecimal calculate(BigDecimal total);
}
