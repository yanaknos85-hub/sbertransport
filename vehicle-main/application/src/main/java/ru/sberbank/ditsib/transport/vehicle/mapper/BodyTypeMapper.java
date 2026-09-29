package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.vehicle.database.model.BodyType;
import ru.sberbank.ditsib.transport.vehicle.dto.bodytype.BodyTypeDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BodyTypeMapper {
    BodyTypeDto bodyTypeToBodyTypeDto(BodyType source);

    List<BodyTypeDto> listBodyTypeToBodyListBodyTypeDto(List<BodyType> source);
}
