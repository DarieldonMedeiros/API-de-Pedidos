package com.darieldon.pedidos.service;

import com.darieldon.pedidos.dto.request.OrderItemDTO;
import com.darieldon.pedidos.dto.request.OrderRequestDTO;
import com.darieldon.pedidos.dto.request.UpdateStatusDTO;
import com.darieldon.pedidos.dto.response.OrderCreatedDTO;
import com.darieldon.pedidos.dto.response.OrderResponseDTO;
import com.darieldon.pedidos.exception.ResourceNotFoundException;
import com.darieldon.pedidos.mapper.OrderMapper;
import com.darieldon.pedidos.model.Order;
import com.darieldon.pedidos.model.OrderStatus;
import com.darieldon.pedidos.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private OrderMapper mapper;

    @InjectMocks
    private OrderServiceImpl service;

    // Dados que serão reutilizados nos testes

    private OrderItemDTO itemDTO() {
        return new OrderItemDTO(1L, 2, new BigDecimal("49.95"));
    }

    private OrderRequestDTO requestDTO() {
        return new OrderRequestDTO("Darieldon Medeiros", List.of(itemDTO()));
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

    private OrderResponseDTO responseDTO() {
        return new OrderResponseDTO(
                1L, "Darieldon Medeiros", List.of(itemDTO()),
                OrderStatus.PENDING, new BigDecimal("99.90"),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    // Testes de Criação

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Deve criar pedido e calcular total corretamente")
        void shouldCreateOrderWithCorrectTotal(){
            Order entity = orderEntity();
            when(mapper.toEntity(any(OrderRequestDTO.class), eq(10L))).thenReturn(entity);
            when(repository.save(any(Order.class))).thenReturn(entity);

            OrderCreatedDTO result = service.create(requestDTO(), 10L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.message()).isEqualTo("Pedido criado com sucesso.");
            verify(repository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("Deve calcular total como soma de quantity * unitPrice")
        void shouldCalculateTotalCorrectly(){
            Order entity = orderEntity();
            when(mapper.toEntity(any(), any())).thenReturn(entity);
            when(repository.save(any())).thenAnswer(invocation -> {
                Order saved = invocation.getArgument(0);
                // Verifica se o total foi setado antes do save
                assertThat(saved.getTotalAmount()).isEqualTo(new BigDecimal("99.90"));
                return saved;
            });

            service.create(requestDTO(), 10L);

            verify(repository).save(any(Order.class));
        }

        @Test
        @DisplayName("Deve calcular total zero quando lista dos items é vazia")
        void shouldCalculateZeroTotalForEmptyList(){
            OrderRequestDTO emptyRequest = new OrderRequestDTO(
                    "Rebeca Castro", List.of()
            );
            Order entity = Order.builder().id(2L)
                    .totalAmount(BigDecimal.ZERO).build();
            when(mapper.toEntity(any(), any())).thenReturn(entity);
            when(repository.save(any())).thenReturn(entity);

            OrderCreatedDTO result = service.create(emptyRequest, 10L);

            assertThat(result.id()).isEqualTo(2L);
        }
    }

    // Testes por busca de ID

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Deve retornar DTO quando pedido existe")
        void shouldReturnDTOWhenOrderExists(){
            Order entity = orderEntity();
            when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(entity));
            when(mapper.toDTO(entity)).thenReturn(responseDTO());

            OrderResponseDTO result = service.findById(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.customerName()).isEqualTo("Darieldon Medeiros");
        }

        @Test
        @DisplayName("Deve retornar ResourceNotFoundException quando pedido não existe")
        void shouldThrowResourceNotFoundExceptionWhenOrderNotFound(){
            when(repository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando pedido foi deletado")
        void shouldThrowResourceNotFoundExceptionWhenOrderSoftDeleted(){
            when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // Testes de listagem

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("Deve retornar página de pedidos sem filtro de status")
        void shouldReturnPageWithoutStatusFilter(){
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> page = new PageImpl<>(List.of(orderEntity()));
            when(repository.findAllByDeletedAtIsNull(pageable)).thenReturn(page);
            when(mapper.toDTOPage(page)).thenReturn(new PageImpl<>(List.of(responseDTO())));

            Page<OrderResponseDTO> result = service.findAll(pageable, null);

            assertThat(result.getContent()).hasSize(1);
            verify(repository).findAllByDeletedAtIsNull(pageable);
            verify(repository, never()).findAllByStatusAndDeletedAtIsNull(any(), any());

        }

        @Test
        @DisplayName("Deve filtrar por status quando informado")
        void shouldFilterByStatusWhenProvided(){
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> page = new PageImpl<>(List.of(orderEntity()));
            when(repository.findAllByStatusAndDeletedAtIsNull(OrderStatus.PENDING, pageable)).thenReturn(page);
            when(mapper.toDTOPage(page)).thenReturn(new PageImpl<>(List.of(responseDTO())));

            Page<OrderResponseDTO> result = service.findAll(pageable, OrderStatus.PENDING);

            assertThat(result.getContent()).hasSize(1);
            verify(repository).findAllByStatusAndDeletedAtIsNull(OrderStatus.PENDING, pageable);
            verify(repository, never()).findAllByDeletedAtIsNull(any());
        }
    }

    // Testes de atualização de status

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("Deve atualizar status do pedido com sucesso")
        void shouldUpdateStatusCorrectly(){
            Order entity = orderEntity();
            when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(entity));
            when(repository.save(entity)).thenReturn(entity);

            service.updateStatus(1L, new UpdateStatusDTO(OrderStatus.PROCESSING));

            assertThat(entity.getStatus()).isEqualTo(OrderStatus.PROCESSING);
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar status de pedido inexistente")
        void shouldThrowWhenUpdatingNonExistentOrder(){
            when(repository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateStatus(99L, new UpdateStatusDTO(OrderStatus.PROCESSING)))
                    .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("99");
        }
    }

    // Testes de soft delete
    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Deve fazer soft delete setando deletedAt")
        void shouldSoftDeleteOrder(){
            Order entity = orderEntity();
            assertThat(entity.getDeletedAt()).isNull(); // garante que a variável inicia null
            when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(entity));

            service.deleteById(1L);

            assertThat(entity.getDeletedAt()).isNotNull();
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar pedido inexistente")
        void shouldTrowWhenDeletingNonExistentOrder(){
            when(repository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteById(99L))
                    .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("99");
        }
    }


}
