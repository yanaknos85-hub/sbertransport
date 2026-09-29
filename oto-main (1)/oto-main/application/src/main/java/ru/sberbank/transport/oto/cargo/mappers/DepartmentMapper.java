package ru.sberbank.transport.oto.cargo.mappers;

import org.mapstruct.*;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.transport.oto.cargo.database.model.Department;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartmentMapper {

    Department fromMessage(DepartmentMessage message);

    @Mapping(target = "id", ignore = true)
    Department update(Department source, @MappingTarget Department target);

}
