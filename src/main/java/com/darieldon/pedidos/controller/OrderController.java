package com.darieldon.pedidos.controller;

import com.darieldon.pedidos.dto.request.OrderRequestDTO;
import com.darieldon.pedidos.dto.request.UpdateStatusDTO;
import com.darieldon.pedidos.dto.response.OrderCreatedDTO;
import com.darieldon.pedidos.dto.response.OrderResponseDTO;
import com.darieldon.pedidos.model.OrderStatus;
import com.darieldon.pedidos.model.User;
import com.darieldon.pedidos.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Gerenciamento de Pedidos")
@SecurityRequirement(name = "Bearer Auth")
public class OrderController {

    private final OrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar Pedido", description = "Cria um novo pedido e retorna o ID gerado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos."),
            @ApiResponse(responseCode = "401", description = "Não autenticado.")
    })
    public OrderCreatedDTO create(@Valid @RequestBody OrderRequestDTO dto, @AuthenticationPrincipal User user) {
        return service.create(dto, user.getId());
    }

    @GetMapping
    @Operation(summary = "Listar pedidos",
            description = "Retorna pedidos com filtro opcional por status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autenticado.")
    })
    public Page<OrderResponseDTO> findAll(
            @PageableDefault(sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) OrderStatus status) {
        return service.findAll(pageable, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public OrderResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Atualizar status do pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public Map<String, String> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusDTO dto) {
        service.updateStatus(id, dto);
        return Map.of("message", "Status atualizado com sucesso.");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar pedido",
            description = "Realiza o soft delete - o pedido não é removido do banco")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido deletado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public Map<String, String> delete(@PathVariable Long id) {
        service.deleteById(id);
        return Map.of("message", "Pedido com o id: " + id + " deletado com sucesso.");
    }

}
