package ru.sber.transport.notifications.messaging.listeners.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.messages.corporate.avro.DepartmentMessage;
import ru.sber.transport.notifications.database.model.coprorate.Department;

/**
 * Маппер подразделений.
 */
@Mapper
public interface DepartmentMapper {

    /**
     * Обновить данные.
     *
     * @param target цель.
     * @param source источник.
     */
    @Mapping(target = "departmentHeadId", source = "headId")
    void update(@MappingTarget Department target, DepartmentMessage source);

}
