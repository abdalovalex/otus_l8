package org.example.billingservice.mapper;

import org.example.billingservice.dto.CreateAccountDTO;
import org.example.billingservice.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {
    @Mapping(target = "id", ignore = true)
    Account map(CreateAccountDTO dto);
}
