package ru.sberbank.ditsib.transport.request.dto.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.PassengerDTO;


/**
 * Mapper for converting  entity to dto and back
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class PassengerDTOMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(expression = "java(employee.getFIO())", target = "name")
    @Mapping(source = "mobilePhone", target = "phone")
    abstract PassengerDTO toDTO(Employee employee);
}
