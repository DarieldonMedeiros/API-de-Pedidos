package com.darieldon.pedidos.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderRequestDTO(
        @NotBlank(message = "Nome do cliente é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String customerName,

        @NotEmpty(message = "Pedido deve ter ao menos um item")
        List<@Valid OrderItemDTO> items
) {}
