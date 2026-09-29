package ru.sber.transport.token_generator.messaging.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.sber.transport.roles.messages.RoleMessage;
import ru.sber.transport.token_generator.database.token_generator.tables.records.RolesRecord;

@Mapper
public interface RolesMapper {

    void update(@MappingTarget RolesRecord role, RoleMessage message);

}
