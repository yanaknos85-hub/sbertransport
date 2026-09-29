package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sberbank.ditsib.transport.messaging.messages.trip.TaxiTripMessage;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CarInfo;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TaxiTripMapper {
    
    //@Mapping(target = "sharedRequest.magentaId", source = "message.sharedRideId")
    CoopTaxiTrip messageToCoopTaxiTrip(TaxiTripMessage message);
    
    @Mapping(target = "id", source = "id", ignore = true)
    void copyToCoopTaxiTrip(TaxiTripMessage message, @MappingTarget CoopTaxiTrip coopTaxiTrip);
    
    SingleTaxiTrip messageToSingleTaxiTrip(TaxiTripMessage message);
    
    @Mapping(target = "id", source = "id", ignore = true)
    void copyToSingleTaxiTrip(TaxiTripMessage message, @MappingTarget SingleTaxiTrip singleTaxiTrip);
    
    @Mapping(target = "sharedRideId", source = "sharedRide.id")
    @Mapping(target = "tariffId", source = "coopTaxiTrip.tariff.id")
    TaxiTripMessage toMessage(CoopTaxiTrip coopTaxiTrip);
    
    @Mapping(target = "brand", source = "brandName")
    @Mapping(target = "stateNumber", source = "registrationNumber")
    TaxiTripMessage.Vehicle toMessage(CarInfo vehicle);
    
    @Mapping(target = "brandName", source = "brand")
    @Mapping(target = "registrationNumber", source = "stateNumber")
    CarInfo fromMessage(TaxiTripMessage.Vehicle vehicle);
    
    @Mapping(target = "requestId", source = "singleTaxiTrip.request.id")
    @Mapping(target = "tariffId", source = "singleTaxiTrip.tariff.id")
    TaxiTripMessage toMessage(SingleTaxiTrip singleTaxiTrip);
}
