package com.darieldon.pedidos.controller;

import com.darieldon.pedidos.dto.request.OrderItemDTO;
import com.darieldon.pedidos.dto.request.OrderRequestDTO;
import com.darieldon.pedidos.dto.request.UpdateStatusDTO;
import com.darieldon.pedidos.dto.response.OrderCreatedDTO;
import com.darieldon.pedidos.dto.response.OrderResponseDTO;
import com.darieldon.pedidos.exception.ResourceNotFoundException;
import com.darieldon.pedidos.model.ClientType;
import com.darieldon.pedidos.model.OrderStatus;
import com.darieldon.pedidos.model.User;
import com.darieldon.pedidos.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderService orderService;

    // Dados reutilizáveis

    private User mockUser() {
        return User.builder()
                .id(10L)
                .email("darieldon@test.com")
                .password("password")
                .role("ROLE_USER")
                .build();
    }

    private OrderItemDTO itemDTO() {
        return new OrderItemDTO(1L, 2, new BigDecimal("49.95"));
    }

    private OrderRequestDTO requestDTO() {
        return new OrderRequestDTO("Darieldon Medeiros", ClientType.NORMAL, List.of(itemDTO()));
    }

    private OrderResponseDTO responseDTO() {
        return new OrderResponseDTO(
                1L,
                "Darieldon Medeiros",
                List.of(itemDTO()),
                OrderStatus.PENDING,
                new BigDecimal("99.90"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // Teste das requisições POST: /orders

    @Nested
    @DisplayName("POST: /orders")
    class CreateOrder {

        @Test
        @DisplayName("Deve criar pedido e retornar 201")
        void shouldCreateOrderAndReturn201() throws Exception {
            when(orderService.create(any(OrderRequestDTO.class), any(Long.class)))
                    .thenReturn(new OrderCreatedDTO(1L, "Pedido criado com sucesso."));

            mockMvc.perform(post("/orders")
                            .with(csrf())
                            .with(user(mockUser()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO())))
                            .andDo(print())
                            .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.message")
                            .value("Pedido criado com sucesso."));

            verify(orderService, times(1))
                    .create(any(OrderRequestDTO.class), any(Long.class));
        }

        @Test
        @DisplayName("Deve passar o userId do usuário autenticado para o service")
        void shouldPassAuthenticatedUserIdToService() throws Exception {

            when(orderService.create(any(OrderRequestDTO.class), eq(10L)))
                    .thenReturn(new OrderCreatedDTO(1L, "Pedido criado com sucesso."));

            mockMvc.perform(post("/orders")
                    .with(csrf())
                    .with(user(mockUser()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO())))
                    .andExpect(status().isCreated());

            verify(orderService).create(any(OrderRequestDTO.class), eq(10L));
        }

        @Test
        @DisplayName("Deve retornar 400 quando customerName está vazio")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            OrderRequestDTO invalid = new OrderRequestDTO("", ClientType.NORMAL, List.of(itemDTO()));

            mockMvc.perform(post("/orders")
                            .with(csrf())
                            .with(user(mockUser()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(orderService, never()).create(any(), any());

        }

        @Test
        @DisplayName("Deve retornar 400 quando lista de itens está vazia")
        void shouldReturn400WhenItemsIsEmpty() throws Exception {
            OrderRequestDTO invalid = new OrderRequestDTO("Darieldon Medeiros", ClientType.NORMAL, List.of());

            mockMvc.perform(post("/orders")
                            .with(csrf())
                            .with(user(mockUser()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Deve retornar 401 quando não autenticado")
        void shouldReturn401WhenUserIsNotAuthenticated() throws Exception {

            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Deve retornar 400 quando ClientType está ausente no JSON")
        void shouldReturn400WhenClientTypeIsMissing() throws Exception {
            String body = """
                    {
                        "customerName": "Darieldon Medeiros",
                        "items": [{"productId": 1, "quantity": 2, "unitPrice": 49.95}]
                    }
                    """;

            mockMvc.perform(post("/orders")
                            .with(csrf())
                            .with(user(mockUser()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                            .andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.message").exists());

            verify(orderService, never()).create(any(), any());
        }

        @Test
        @DisplayName("Deve retornar 400 quando o clientType não é um valor do enum")
        void shouldReturn400WhenClientTypeIsInvalidEnum() throws Exception {
            String body = """
                    {
                        "customerName": "Darieldon Medeiros",
                        "clientType": "GOLD",
                        "items": [{"productId": 1, "quantity": 2, "unitPrice": 49.95}]
                    }
                    """;

            mockMvc.perform(post("/orders")
                    .with(csrf())
                    .with(user(mockUser()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(orderService, never()).create(any(), any());

        }
    }

    // Teste das requisições GET: /orders

    @Nested
    @DisplayName("GET: /orders")
    class FindAllOrders {

        @Test
        @WithMockUser
        @DisplayName("Deve retornar página de pedidos com status 200")
        void shouldReturnPageOfOrders() throws Exception {
            when(orderService.findAll(any(Pageable.class), eq(null)))
                    .thenReturn(new PageImpl<>(List.of(responseDTO())));

            mockMvc.perform(get("/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[0].customerName")
                            .value("Darieldon Medeiros"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(orderService).findAll(any(Pageable.class), eq(null));
        }

        @Test
        @WithMockUser
        @DisplayName("Deve filtrar pedidos por status quando informado")
        void shouldFilterByStatusWhenProvided() throws Exception {

            when(orderService.findAll(any(Pageable.class), eq(OrderStatus.PENDING)))
                    .thenReturn(new PageImpl<>(List.of(responseDTO())));

            mockMvc.perform(get("/orders")
                    .param("status", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].status").value("PENDING"));

            verify(orderService).findAll(any(Pageable.class), eq(OrderStatus.PENDING));
        }

        @Test
        @DisplayName("Deve retornar 401 quando não autenticado")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/orders"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // Teste das requisições GET: /orders/{id}

    @Nested
    @DisplayName("GET: /orders/{id}")
    class FindOrderById {

        @Test
        @WithMockUser
        @DisplayName("Deve retornar pedido quando existe")
        void shouldReturnOrderWhenExists() throws Exception {

            when(orderService.findById(1L)).thenReturn(responseDTO());

            mockMvc.perform(get("/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.customerName").value("Darieldon Medeiros"))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.totalAmount").value("99.90"));
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 404 quando o pedido não existe")
        void shouldReturn404WhenOrderNotFound() throws Exception {

            Long idNotFound = 99L;
            when(orderService.findById(idNotFound))
                    .thenThrow(new ResourceNotFoundException(
                            "Pedido com o id =  " + idNotFound + " não encontrado!"));

            mockMvc.perform(get("/orders/" + idNotFound))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Pedido com o id =  " + idNotFound + " não encontrado!"));
        }

        @Test
        @DisplayName("Deve retornar 401 quando não autenticado")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/orders/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // Teste das requisições PUT: /orders/{id}/status

    @Nested
    @DisplayName("PUT /orders/{id}/status")
    class UpdateOrderStatus {

        @Test
        @WithMockUser
        @DisplayName("Deve atualizar status e retornar mensagem de sucesso")
        void shouldUpdateStatusSuccessfully() throws Exception {

            doNothing().when(orderService)
                    .updateStatus(eq(1L), any(UpdateStatusDTO.class));

            mockMvc.perform(put("/orders/1/status")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new UpdateStatusDTO(OrderStatus.PROCESSING))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Status atualizado com sucesso."));
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 404 quando o pedido não existe")
        void shouldReturn404WhenOrderNotFound() throws Exception {

            Long idNotFound = 99L;
            doThrow(new ResourceNotFoundException("Pedido não encontrado com id = " + idNotFound))
                    .when(orderService)
                    .updateStatus(eq(idNotFound), any(UpdateStatusDTO.class));

            mockMvc.perform(put("/orders/" + idNotFound + "/status")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new UpdateStatusDTO(OrderStatus.PROCESSING))))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 400 quando status é nulo")
        void  shouldReturn400WhenStatusIsNull() throws Exception {
            mockMvc.perform(put("/orders/1/status")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\": null}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 401 quando não autenticado")
        void shouldReturn401WhenNotAuthenticated() throws Exception {

            mockMvc.perform(get("/orders/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new UpdateStatusDTO(OrderStatus.PROCESSING))))
                    .andExpect(status().isUnauthorized());
        }
    }

    // Teste das requisições DELETE: /orders/{id}

    @Nested
    @DisplayName("DELETE: /orders/{id}")
    class DeleteOrder {

        @Test
        @WithMockUser
        @DisplayName("Deve deletar pedido e retornar mensagem de sucesso")
        void shouldDeleteOrderSuccessfully() throws Exception {
            Long idDelete = 1L;
            doNothing().when(orderService).deleteById(idDelete);

            mockMvc.perform(delete("/orders/" + idDelete)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Pedido com o id: " + idDelete + " deletado com sucesso."));

            verify(orderService, times(1)).deleteById(idDelete);
        }
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 quando o pedido não existe")
    void shouldReturn404WhenOrderNotFound() throws Exception {

        Long idNotFound = 99L;
        doThrow(new ResourceNotFoundException("Pedido com o id =  " + idNotFound + " não encontrado!"))
                .when(orderService).deleteById(idNotFound);

        mockMvc.perform(delete("/orders/" + idNotFound)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Pedido com o id =  " + idNotFound + " não encontrado!"));
    }

    @Test
    @DisplayName("Deve retornar 401 quando não autenticado")
    void shouldReturn401WhenNotAuthenticated() throws Exception {

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isUnauthorized());
    }
}
