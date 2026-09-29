package ru.sberbank.transport.oto.cargo.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.transport.oto.cargo.dto.EmployeeDTO;
import ru.sberbank.transport.oto.cargo.database.model.Employee;

@Mapper
public interface EmployeeMapper {
    
    EmployeeDTO toDto(Employee source);
    
}
