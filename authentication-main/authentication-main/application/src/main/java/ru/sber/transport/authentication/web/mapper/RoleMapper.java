package ru.sber.transport.authentication.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.authentication.business.dto.RoleDto;
import ru.sber.transport.authentication.web.model.Role;

/**
 * Маппинг токенов из бизнес в веб.
 */
@Mapper
public interface RoleMapper {
    
    @Mapping(target = "description", source = "description")
    @Mapping(target = "isDefault", ignore = true)
    Role toDto(RoleDto source);
    
}
