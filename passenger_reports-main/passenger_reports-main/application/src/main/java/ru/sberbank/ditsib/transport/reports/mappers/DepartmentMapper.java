package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.reports.dto.DepartmentShortDTO;
import ru.sberbank.ditsib.transport.reports.model.Department;

@Mapper
public interface DepartmentMapper {

    Department fromMessage(DepartmentMessage message);

    @Mapping(target = "id", ignore = true)
    Department update(Department source, @MappingTarget Department target);
    
    DepartmentShortDTO toDto(Department department);

}
