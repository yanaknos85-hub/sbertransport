package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.telemechanic.database.model.Department;
import ru.sber.transport.telemechanic.dto.DepartmentDto;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Маппер подразделений.
 */
@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(source = "organizationId", target = "organization.id")
    Department departmentMessageToDepartment(DepartmentMessage source);
    
    List<DepartmentDto> departmentListToDepartmentDtoList(List<Department> source);
    
    default Department map(UUID value) {
        return Objects.isNull(value) ? null : Department.builder()
                                                        .id(value)
                                                        .build();
    }
}
