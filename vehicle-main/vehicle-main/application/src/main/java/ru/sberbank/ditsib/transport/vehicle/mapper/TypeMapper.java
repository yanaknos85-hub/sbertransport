package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.vehicle.database.model.Type;
import ru.sberbank.ditsib.transport.vehicle.dto.type.TypeDto;

import java.util.List;

/**
 * @author skakun-a
 */
@Mapper(componentModel = "spring")
public interface TypeMapper {
    TypeDto typeToTypeDto(Type source);
    
    Type typeDtoToType(TypeDto source);
    
    List<TypeDto> listTypeToListTypeDto(List<Type> source);
}
