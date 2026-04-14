package com.darieldon.pedidos.mapper;

import com.darieldon.pedidos.dto.request.OrderRequestDTO;
import com.darieldon.pedidos.dto.response.OrderResponseDTO;
import com.darieldon.pedidos.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "items",  expression = "java(fromJson(order.getItems()))")
    OrderResponseDTO toDTO(Order order);

    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Order toEntity(OrderRequestDTO dto, Long userId);

    default Page<OrderResponseDTO> toDTOPage(Page<Order> page){
        return page.map(this::toDTO);
    }
}
