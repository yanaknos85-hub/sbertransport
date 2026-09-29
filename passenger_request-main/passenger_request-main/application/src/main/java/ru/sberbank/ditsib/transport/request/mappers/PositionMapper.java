package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.sberbank.ditsib.transport.request.database.model.corp.Position;
import ru.sberbank.ditsib.transport.request.dto.EmployeeDTO;

@Mapper
public interface PositionMapper {
    
    void update(@MappingTarget EmployeeDTO employeeDTO, Position position);
    
}
