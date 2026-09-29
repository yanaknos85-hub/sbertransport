package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.contractor.messages.ContractorUpdateTripMessage;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sberbank.ditsib.transport.request.database.model.CarInfo;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Vehicle;
import ru.sberbank.ditsib.transport.request.dto.VehicleDTO;
import ru.sberbank.ditsib.transport.request.messaging.message.TaxiTripMessage;

@Mapper
public interface VehicleMapper {
    
    Vehicle map(ContractorUpdateTripMessage.Vehicle vehicle);
    
    @Mapping(target = "brand", source = "mark" )
    @Mapping(target = "stateNumber", source = "registrationNumber" )
    Vehicle map(InContractorTaxiTripInProgressMessage.Vehicle vehicle);
    
    @Mapping(target = "brand", source = "brandName" )
    @Mapping(target = "stateNumber", source = "registrationNumber" )
    TaxiTripMessage.Vehicle toMessage(CarInfo vehicle);
    
    @Mapping(target = "brandName", source = "brand" )
    @Mapping(target = "registrationNumber", source = "stateNumber" )
    CarInfo fromMessage(TaxiTripMessage.Vehicle vehicle);
    
    @Mapping(target = "brand", source = "brandName" )
    @Mapping(target = "stateNumber", source = "registrationNumber" )
    @Mapping(target = "name", source = "model" )
    VehicleDTO toVehicleDto(CarInfo carInfo);
    
    void update(@MappingTarget Vehicle vehicle, ContractorUpdateTripMessage.Vehicle receivedVehicle);
    
}
