package com.darieldon.pedidos.dto.request;

import com.darieldon.pedidos.model.ClientType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderRequestDTO(
        @NotBlank(message = "Nome do cliente é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String customerName,

        @NotNull(message = "Tipo do cliente é obrigatório para o cálculo de desconto")
        ClientType clientType,

        @NotEmpty(message = "Pedido deve ter ao menos um item")
        List<@Valid OrderItemDTO> items
) {}
