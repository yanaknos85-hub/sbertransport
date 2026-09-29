package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.*;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sber.transport.telemechanic.database.model.Department;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.dto.EmployeeDto;
import ru.sber.transport.telemechanic.dto.medic.GetMedicDto;

/**
 * Маппер сотрудников.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = Department.class,
        builder = @Builder(disableBuilder = true))
public interface EmployeeMapper {
    
    @Mapping(source = "departmentId", target = "department.id")
    @Mapping(source = "positionId", target = "position.id")
    @Mapping(source = "organizationId", target = "organization.id")
    Employee employeeMessageToEmployee(EmployeeMessage source);
    
    @Mapping(source = "organization.officialName", target = "organizationOfficialName")
    @Mapping(source = "department.organization.id", target = "organizationId")
    EmployeeDto employeeToEmployeeDto(Employee source);
    
    @Mapping(target = "fullName", source = "FIO")
    @Mapping(target = "positionName", source = "position.positionName")
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationName", source = "organization.officialName")
    GetMedicDto employeeToGetMedicDto(Employee employee);
}
