package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.request.messaging.message.ContractMessage;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingContract;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CarsharingContractMapper {
    
    CarsharingContract messageToEntity(ContractMessage message);
}
