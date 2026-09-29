package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.*;
import ru.sberbank.ditsib.transport.integrations.carsharing.messages.CarsharingDataMessage;
import ru.sberbank.ditsib.transport.reports.model.CarsharingTrip;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CarsharingTripMapper {
    
    @Mapping(target = "id", ignore = true)
    CarsharingTrip toModel(CarsharingDataMessage message, @MappingTarget CarsharingTrip target);
    
}
