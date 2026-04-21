package com.darieldon.pedidos.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("NORMAL")
public class NormalDiscount implements DiscountStrategy {

    @Override
    public BigDecimal calculate(BigDecimal total) {
        return total;
    }
}
