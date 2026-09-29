package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.messaging.message.TariffMessage;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingTariff;

import java.util.UUID;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CarsharingTariffMapper {
    
    @Mapping(target = "transportType", source = "message.transportTypeId")
    CarsharingTariff messageToEntity(TariffMessage message);
    
    default TransportTypeEnum toEnum(UUID transportTypeId){
        return TransportTypeEnum.fromId(transportTypeId).orElseThrow(
                ()-> new EntityNotFoundException(TransportTypeEnum.class, transportTypeId));
    }
}
