package org.example.billingservice.mapper;

import org.example.billingservice.dto.IncomeMoneyDTO;
import org.example.billingservice.entity.Operation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OperationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "type", ignore = true)
    Operation map(IncomeMoneyDTO dto);
}
