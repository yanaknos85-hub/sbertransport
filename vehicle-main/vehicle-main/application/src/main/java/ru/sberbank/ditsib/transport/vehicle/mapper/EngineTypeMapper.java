package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.vehicle.database.model.EngineType;
import ru.sberbank.ditsib.transport.vehicle.dto.enginetype.EngineTypeDto;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.EngineTypeMessage;

import java.util.List;

/**
 * @author skakun-a
 */
@Mapper(componentModel = "spring")
public interface EngineTypeMapper {
    EngineTypeDto engineTypeToEngineTypeDto(EngineType entity);
    
    EngineType engineTypeDtoToEngineType(EngineTypeDto dto);
    
    List<EngineTypeDto> listEngineTypeToListEngineTypeDro(List<EngineType> entities);

    @Mapping(target = "deleted", source = "deleted")
    EngineTypeMessage engineTypeToEngineTypeMessage(EngineType source, boolean deleted);
}
