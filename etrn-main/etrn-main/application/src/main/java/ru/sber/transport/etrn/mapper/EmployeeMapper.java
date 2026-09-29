package ru.sber.transport.etrn.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.sber.transport.etrn.database.model.Employee;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    Employee fromMessage(EmployeeMessage message);

    @Mapping(target = "id", ignore = true)
    Employee update(Employee source, @MappingTarget Employee target);

    @Mapping(source = "departmentId", target = "department.id")
    Employee employeeMessageToEmployeeEntity(EmployeeMessage employeeMessage);
}