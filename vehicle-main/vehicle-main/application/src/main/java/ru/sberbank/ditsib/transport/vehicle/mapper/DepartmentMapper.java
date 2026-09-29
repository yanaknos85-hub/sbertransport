package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.vehicle.database.model.Department;
import ru.sberbank.ditsib.transport.vehicle.dto.DepartmentDto;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Маппер подразделений.
 */
@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(source = "parentId", target = "parent")
    Department departmentMessageToDepartment(DepartmentMessage source);

    @Mapping(source = "parent.id", target = "parentId")
    DepartmentDto departmentToDepartmentDto(Department source);

    Set<UUID> departmentsToDepartmentUUIDs(Set<Department> departments);

    default UUID departmentToDepartmentUUID(Department department){
        return department != null ? department.getId() : null;
    }

    default Department uuidToDepartment(UUID value) {
        return Objects.isNull(value) ? null : Department.builder()
                .id(value)
                .build();
    }
}
