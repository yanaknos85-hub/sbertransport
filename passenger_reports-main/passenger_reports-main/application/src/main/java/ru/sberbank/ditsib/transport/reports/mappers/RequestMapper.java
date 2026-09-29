package ru.sberbank.ditsib.transport.reports.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.tariff.BaseTariff;

import java.util.Collections;
import java.util.Map;

@Mapper(uses = { ExpectedDataMapper.class, DateTimeMapper.class })
public interface RequestMapper {
    
    @Mapping(source = "request.id", target = "id")
    @Mapping(source = "request.humanReadableId", target = "humanReadableId")
    @Mapping(source = "request.author.id", target = "authorId")
    @Mapping(source = "request.passenger.id", target = "passengerId")
    @Mapping(source = "request.creationTime", target = "creationTime")
    @Mapping(source = "request.transportType", target = "transportType")
    @Mapping(source = "request.transportClass", target = "tripClass")
    @Mapping(source = "request.waypoints", target = "waypoints")
    @Mapping(source = "request.approvedBy.id", target = "approvalId")
    @Mapping(source = "request.approvalDate", target = "approvalDate")
    @Mapping(source = "request.tariff.id", target = "tariffId")
    @Mapping(source = "request.coopTrip", target = "coopTrip", defaultValue = "false")
    @Mapping(source = "request.desiredDate", target = "desiredDate")
    @Mapping(source = "request.status", target = "status")
    @Mapping(source = "request.purpose.id", target = "purposeId")
    @Mapping(source = "request.commentForDriver", target = "commentForDriver")
    @Mapping(source = "request.contractor.id", target = "contractorId")
    @Mapping(source = "request.carsharingClass", target = "carsharingClass")
    @Mapping(source = "request.autopark.id", target = "autoparkId")
    @Mapping(source = "request.driver.id", target = "driverId")
    @Mapping(source = "request.personalCar.id", target = "personalCarId")
    @Mapping(target = "request.tariff", expression = "java(objectToMap(request.getTariff()))")
    RequestMessage toMessage(Request request);
    
    
    default Map<String, Object> baseTariffToJson(BaseTariff tariff) {
        if (tariff != null) {
            return objectMapper().convertValue(tariff, new TypeReference<>() {
            });
        } else {
            return Collections.emptyMap();
        }
    }
    
    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }
}

