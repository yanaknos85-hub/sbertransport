package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.vehicle.database.model.Telematics;
import ru.sberbank.ditsib.transport.vehicle.dto.telematics.TelematicsDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TelematicsMapper {

    TelematicsDto telematicsToTelematicsDto(Telematics source);

    List<TelematicsDto> listTelematicsToListTelematicsDto(List<Telematics> sourceList);
}
