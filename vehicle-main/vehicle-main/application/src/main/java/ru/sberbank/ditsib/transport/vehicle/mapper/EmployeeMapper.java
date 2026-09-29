package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.*;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.vehicle.database.model.Department;
import ru.sberbank.ditsib.transport.vehicle.database.model.Employee;
import ru.sberbank.ditsib.transport.vehicle.dto.EmployeeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.telematics.TelemechanicDto;

/**
 * Маппер сотрудников.
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = Department.class,
        builder = @Builder(disableBuilder = true))
public interface EmployeeMapper {
    
    @Mapping(source = "departmentId", target = "department.id")
    @Mapping(source = "positionId", target = "position.id")
    @Mapping(source = "organizationId", target = "organization.id")
    Employee employeeMessageToEmployee(EmployeeMessage source);

    @Mapping(target = "fullName", source = "FIO")
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationName", source = "organization.officialName")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.departmentName")
    TelemechanicDto employeeToTelemechanicDto(Employee source);


    @Mapping(target = "fullName", source = "FIO")
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationName", source = "organization.officialName")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.departmentName")
    EmployeeDto employeeToEmployeeDto(Employee employee);
}
