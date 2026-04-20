package com.darieldon.pedidos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PedidosApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	@DisplayName("Deve executar o método main da aplicação")
	void mainTest(){
		PedidosApplication.main(new String[]{});
	}

}
