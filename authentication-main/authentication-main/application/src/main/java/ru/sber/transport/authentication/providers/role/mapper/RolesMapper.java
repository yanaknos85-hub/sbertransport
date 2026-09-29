package ru.sber.transport.authentication.providers.role.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.authentication.business.dto.RoleDto;
import ru.sber.transport.database.authentication.tables.records.RoleRecord;
import ru.sber.transport.roles.messages.RoleMessage;

/**
 * Маппер ролей.
 */
@Mapper
public interface RolesMapper {
    
    RoleDto toBusiness(RoleRecord role);

    RoleRecord toModel(RoleMessage message);
}
