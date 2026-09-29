package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.vehicle.database.model.OdometerValue;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.GetIndicatorValueDto;

@Mapper(componentModel = "spring")
public interface IndicatorMapper {
    
    GetIndicatorValueDto odometerValueToGetIndicatorValueDto(OdometerValue source);
}
