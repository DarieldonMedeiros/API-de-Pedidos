package com.darieldon.pedidos.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class VipDiscountTest {

    private final VipDiscount vipDiscount = new VipDiscount();

    @Test
    @DisplayName("Deve aplicar o desconto de 10% sobre o total")
    void shouldApplyTenPercentDiscount() {
        BigDecimal result = vipDiscount.calculate(new BigDecimal("100.00"));

        assertThat(result).isEqualByComparingTo(new BigDecimal("90.00"));
    }

    @Test
    @DisplayName("Deve preservar precisão do BigDecimal retornado pela multiplicação")
    void shouldReturnScaledProduct() {
        BigDecimal result = vipDiscount.calculate(new BigDecimal("49.95"));

        assertThat(result).isEqualByComparingTo(new BigDecimal("44.955"));
    }
}
