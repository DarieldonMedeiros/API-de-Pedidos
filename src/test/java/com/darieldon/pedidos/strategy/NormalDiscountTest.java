package com.darieldon.pedidos.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class NormalDiscountTest {

    private final NormalDiscount normalDiscount = new NormalDiscount();

    @Test
    @DisplayName("Deve retornar o mesmo total (sem desconto)")
    void shouldReturnSameTotal() {
        BigDecimal total = new BigDecimal("49.95");
        assertThat(normalDiscount.calculate(total)).isEqualByComparingTo(total);
    }

    @Test
    @DisplayName("Deve retornar zero quando o total é zero")
    void shouldReturnZeroWhenTotalIsZero() {
        assertThat(normalDiscount.calculate(BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
