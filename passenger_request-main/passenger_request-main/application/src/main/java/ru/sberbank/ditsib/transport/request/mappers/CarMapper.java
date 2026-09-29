package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sberbank.ditsib.transport.request.database.model.CarInfo;

@Mapper
public interface CarMapper {
    
    @Mapping(target = "brandName", source = "mark")
    CarInfo toModel(InContractorTaxiTripInProgressMessage.Vehicle vehicle);
    
}
