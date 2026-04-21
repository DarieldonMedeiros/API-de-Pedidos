package com.darieldon.pedidos.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("VIP")
public class VipDiscount implements DiscountStrategy{

    @Override
    public BigDecimal calculate(BigDecimal total){
        return total.multiply(new BigDecimal("0.90"));
    }
}
