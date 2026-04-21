package com.darieldon.pedidos.mapper;

import com.darieldon.pedidos.dto.request.OrderItemDTO;
import com.darieldon.pedidos.dto.request.OrderRequestDTO;
import com.darieldon.pedidos.dto.response.OrderResponseDTO;
import com.darieldon.pedidos.model.ClientType;
import com.darieldon.pedidos.model.Order;
import com.darieldon.pedidos.model.OrderItem;
import com.darieldon.pedidos.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.*;

class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapperImpl();

    // Dados reutilizáveis

    private OrderItemDTO itemDTO() {
        return new OrderItemDTO(1L, 2, new BigDecimal("49.95"));
    }

    private OrderRequestDTO requestDTO() {
        return new OrderRequestDTO("Darieldon Medeiros", ClientType.NORMAL, List.of(itemDTO()));
    }

    private Order orderEntity(){
        return Order.builder()
                .id(1L)
                .customerName("Darieldon Medeiros")
                .totalAmount(new BigDecimal("99.90"))
                .status(OrderStatus.PENDING)
                .userId(10L)
                .createdAt(LocalDateTime.now())
                .build();
    }


    // Testa mapeamento do RequestDTO -> Entity
    @Nested
    @DisplayName("Mapeamento: RequestDTO -> Entity")
    class RequestDTOToEntity {

        @Test
        @DisplayName("Deve converter OrderRequestDTO em Entity corretamente")
        void shouldMapRequestDTOToEntity() {
            Long userId = 10L;
            OrderRequestDTO requestDTO = requestDTO();

            Order result = mapper.toEntity(requestDTO, userId);

            assertThat(result).isNotNull();
            assertThat(result.getCustomerName()).isEqualTo(requestDTO.customerName());
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getItems().get(0).productId()).isEqualTo(requestDTO.items().get(0).productId());
        }

        @Test
        @DisplayName("Deve retornar o objeto apenas com o UserId quando o DTO for nulo")
        void shouldReturnOrderWithOnlyUserIdWhenDTOIsNull() {
            Long userId = 10L;

            Order result = mapper.toEntity(null, userId);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getCustomerName()).isNull();
            assertThat(result.getItems()).isNull();
        }

        @Test
        @DisplayName("Deve retornar o objeto apenas com o UserId e o CustomerName quando os itens são nulos")
        void shouldMapRequestToEntityWithNullItems() {

            OrderRequestDTO requestDTO = new OrderRequestDTO("Darieldon Medeiros", ClientType.NORMAL,null);

            Order result = mapper.toEntity(requestDTO, 10L);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(10L);
            assertThat(result.getCustomerName()).isEqualTo(requestDTO.customerName());
            assertThat(result.getItems()).isNull();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando os itens são nulos")
        void shouldReturnEmptyListWhenDTOItemsIsEmpty() {

            OrderRequestDTO requestDTO = new OrderRequestDTO(
                    "Darieldon Medeiros", ClientType.NORMAL, emptyList());

            Order result = mapper.toEntity(requestDTO, 10L);

            assertThat(result.getItems()).isEmpty();
        }

        @Test
        @DisplayName("Deve lidar com itens nulos dentro da lista do DTO")
        void shouldHandleNullItemsInDTOList() {

            List<OrderItemDTO> nullItems = new ArrayList<>();
            nullItems.add(null);

            OrderRequestDTO requestDTO = new OrderRequestDTO("Darieldon Medeiros", ClientType.NORMAL, nullItems);

            Order result = mapper.toEntity(requestDTO, 10L);

            assertThat(result.getItems()).containsOnly((OrderItem) null);
        }
    }

    // Testa mapeamento da Entity -> ResponseDTO

    @Nested
    @DisplayName("Mapeamento: Entity -> ResponseDTO")
    class EntityToResponseDTO {

        @Test
        @DisplayName("Deve converter Entity em OrderResponseDTO corretamente")
        void shouldMapEntityToResponseDTO() {
            Order entity = orderEntity();

            OrderResponseDTO result = mapper.toDTO(entity);

            assertThat(result)
                    .isNotNull()
                    .extracting(OrderResponseDTO::id, OrderResponseDTO::customerName, OrderResponseDTO::status)
                    .containsExactly(entity.getId(), entity.getCustomerName(), entity.getStatus());
            assertThat(result.totalAmount()).isEqualByComparingTo("99.90");
        }

        @Test
        @DisplayName("Deve converter Page de Entity em Page de OrderResponseDTO corretamente")
        void shouldMapPageToDTOPage() {

            Order entity = orderEntity();
            Page<Order> orderPage = new PageImpl<>(List.of(entity));

            Page<OrderResponseDTO> result = mapper.toDTOPage(orderPage);

            assertThat(result).isNotEmpty();
            assertThat(result.getContent().get(0).customerName()).isEqualTo(entity.getCustomerName());
        }

        @Test
        @DisplayName("Deve retornar null quando a Entity for nulo")
        void shouldReturnNullWhenEntityIsNull() {

            assertThat(mapper.toDTO(null)).isNull();
        }
    }

    @Nested
    @DisplayName("Mapeamento: métodos auxiliares")
    class HelperMethods {

        @Test
        @DisplayName("Deve converter OrderItem em OrderItemDTO corretamente")
        void shouldMapOrderItemToOrderItemDTO() {

            OrderItem item = new OrderItem(1L, 2, new BigDecimal("49.95"));
            Order entity = Order.builder()
                    .id(1L)
                    .customerName("Darieldon Medeiros")
                    .items(List.of(item))
                    .status(OrderStatus.PENDING)
                    .userId(10L)
                    .createdAt(LocalDateTime.now())
                    .build();

            OrderResponseDTO result = mapper.toDTO(entity);

            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).productId()).isEqualTo(1L);
            assertThat(result.items().get(0).quantity()).isEqualTo(2);
            assertThat(result.items().get(0).unitPrice()).isEqualByComparingTo("49.95");
        }

        @Test
        @DisplayName("Deve retornar null quando OrderItem for nulo no toDTO")
        void shouldReturnNullWhenOrderItemIsNull() {
            Order entity  = Order.builder()
                    .id(1L)
                    .customerName("Darieldon Medeiros")
                    .items(List.of())
                    .status(OrderStatus.PENDING)
                    .userId(10L)
                    .createdAt(LocalDateTime.now())
                    .build();

            OrderResponseDTO result = mapper.toDTO(entity);

            assertThat(result.items()).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar null quando lista de OrderItems for nula no toDTO")
        void shouldReturnNullWhenOrderItemsListIsNull() {

            Order entity = Order.builder()
                    .id(1L)
                    .customerName("Darieldon Medeiros")
                    .items(null)
                    .status(OrderStatus.PENDING)
                    .userId(10L)
                    .createdAt(LocalDateTime.now())
                    .build();

            OrderResponseDTO result = mapper.toDTO(entity);

            assertThat(result.items()).isNull();
        }

        @Test
        @DisplayName("Deve converter lista com múltiplos OrderItems corretamente")
        void shouldMapMultipleOrderItems() {
            List<OrderItem> items = List.of(
                    new OrderItem(1L, 2, new BigDecimal("49.95")),
                    new OrderItem(2L, 1, new BigDecimal("99.90")),
                    new OrderItem(3L, 3, new BigDecimal("19.90"))
            );

            Order entity = Order.builder()
                    .id(1L)
                    .customerName("Darieldon Medeiros")
                    .items(items)
                    .status(OrderStatus.PENDING)
                    .userId(10L)
                    .createdAt(LocalDateTime.now())
                    .build();

            OrderResponseDTO result = mapper.toDTO(entity);

            assertThat(result.items()).hasSize(3);
            assertThat(result.items().get(1).productId()).isEqualTo(2L);
            assertThat(result.items().get(2).quantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("Deve cobrir branch dto != null com userId null no toEntity")
        void shouldMapEntityWithUserIdIsNull() {

            OrderRequestDTO requestDTO = requestDTO();

            Order result = mapper.toEntity(requestDTO, null);

            assertThat(result).isNotNull();
            assertThat(result.getCustomerName()).isEqualTo(requestDTO.customerName());
            assertThat(result.getUserId()).isNull();
        }

        @Test
        @DisplayName("Deve retornar null quando DTO e UserId forem nulos")
        void shouldReturnNullWhenDTOAndUserIdIsNull() {
            Order result = mapper.toEntity(null, null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Deve retornar null quando OrderItem for nulo dentro da lista")
        void shouldReturnNullWhenOrderItemInsideListIsNull() {

            List<OrderItem> nullItems = new ArrayList<>();
            nullItems.add(null);

            Order entity = Order.builder()
                    .id(1L)
                    .customerName("Darieldon Medeiros")
                    .items(nullItems)
                    .status(OrderStatus.PENDING)
                    .userId(10L)
                    .createdAt(LocalDateTime.now())
                    .build();

            OrderResponseDTO result = mapper.toDTO(entity);

            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0)).isNull();
        }
    }
}
