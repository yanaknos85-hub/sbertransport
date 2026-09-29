package ru.sberbank.ditsib.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.database.model.Department;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.Objects;
import java.util.UUID;

/**
 * Маппер подразделений.
 */
@Mapper
public interface DepartmentMapper {

    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(source = "parentId", target = "parent")
    Department departmentMessageToDepartment(DepartmentMessage source);
    
    default Department map(UUID value) {
        return Objects.isNull(value) ? null : Department.builder()
                                                        .id(value)
                                                        .build();
    }
}
