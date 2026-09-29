package ru.sberbank.ditsib.transport.tariff.mappers;

import org.mapstruct.*;

import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;

@Mapper
public interface DepartmentMapper {

    Department fromMessage(DepartmentMessage message);

    @Mapping(target = "id", ignore = true)
    Department update(Department source, @MappingTarget Department target);

}
