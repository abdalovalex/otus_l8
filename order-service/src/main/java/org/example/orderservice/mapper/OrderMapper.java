package org.example.orderservice.mapper;

import org.example.orderservice.dto.OrderDTO;
import org.example.orderservice.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "amount", ignore = true)
    Order map(OrderDTO dto);
}
