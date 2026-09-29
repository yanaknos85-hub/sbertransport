package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import ru.sberbank.ditsib.transport.messaging.messages.trip.CargoRequestMessage;
import ru.sberbank.ditsib.transport.reports.model.cargo.CargoDetail;

import java.util.List;

/**
 * Маппер метрик груза.
 */
@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CargoDetailMapper {
    
    CargoRequestMessage.CargoDetail toMessage(CargoDetail cargoDetail);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<CargoRequestMessage.CargoDetail> toMessage(List<CargoDetail> cargoDetails);
    
    List<CargoDetail> toEntity (List<CargoRequestMessage.CargoDetail> messages);
    
}
