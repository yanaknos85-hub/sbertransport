package ru.sberbank.ditsib.transport.request.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sberbank.ditsib.transport.request.database.model.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.database.model.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.request.database.model.TaxiTrip;
import ru.sberbank.ditsib.transport.request.messaging.message.RequestFactDataMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.TaxiTripMessage;

import java.util.List;
import java.util.UUID;

@Mapper(uses = VehicleMapper.class)
public interface TaxiTripMapper {
    
    @Mapping(target = "requestId", expression = "java(getRequestId(taxiTrip))")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "vehicle", source = "taxiTrip.assignedCar")
    TaxiTripMessage toMessage(SingleTaxiTrip taxiTrip, boolean deleted);
    
    @Mapping(target = "sharedRideId", source = "taxiTrip.rideId")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "vehicle", source = "taxiTrip.assignedCar")
    @Mapping(target = "requestIds", source = "taxiTrip.requests")
    TaxiTripMessage toMessage(CoopTaxiTrip taxiTrip, boolean deleted);
    
    TaxiTripMessage toMessage(TaxiTrip taxiTrip);
    
    
    @Mapping(source = "source.factTotalWaitingTime", target = "target.registryFactWaitingTime")
    @Mapping(source = "source.registryHrId", target = "target.registryHumanReadableId")
    @Mapping(source = "source.factCost", target = "target.registryFactCost")
    @Mapping(source = "source.factDistance", target = "target.registryFactDistance")
    @Mapping(source = "source.isPaid", target = "target.registryFactPayment")
    @Mapping(ignore = true, target = "target.id")
    void toTaxiTrip(@MappingTarget TaxiTrip target, RequestFactDataMessage source);
    
    @SneakyThrows(JsonProcessingException.class)
    default TaxiTripMessage.Driver toMessage(String driver) {
        if (driver == null) {
            return null;
        }
        return objectMapper().readValue(driver, TaxiTripMessage.Driver.class);
    }
    
    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }
    
    default List<UUID> mapRequestsListToRequestIdsList(List<RequestForTaxi> requests) {
        return requests.stream()
                .map(RequestForTaxi::getId)
                .toList();
    }

    default UUID getRequestId(SingleTaxiTrip taxiTrip){
        if (taxiTrip.getRequests() == null || taxiTrip.getRequests().isEmpty()){
            return null;
        }
        return taxiTrip.getRequests().getFirst().getId();
    }
}
