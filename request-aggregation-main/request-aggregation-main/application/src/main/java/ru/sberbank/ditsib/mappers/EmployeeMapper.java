package ru.sberbank.ditsib.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;
import ru.sberbank.ditsib.database.model.Employee;
import ru.sberbank.ditsib.dto.EmployeeDto;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

/**
 * Маппер для конвертации Employee в EmployeeResponseModel
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface EmployeeMapper {

    @Mapping(source = "departmentId", target = "department.id")
    @Mapping(source = "positionId", target = "position.id")
    @Mapping(source = "organizationId", target = "organization.id")
    Employee employeeMessageToEmployee(EmployeeMessage source);

    @Mapping(source = "organization.officialName", target = "organizationOfficialName")
    @Mapping(source = "department.organization.id", target = "organizationId")
    EmployeeDto employeeToEmployeeDto(Employee source);
}