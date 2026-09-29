package ru.sberbank.ditsib.transport.request.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.request.messaging.message.UpdateTripRequestMessage;
import ru.sberbank.ditsib.transport.request.database.model.BaseTariff;
import ru.sberbank.ditsib.transport.request.database.model.UpdateRequest;

import java.util.Collections;
import java.util.Map;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses={ RequestMapper.class})
public interface UpdateRequestMapper {
    @Mapping(target = "request.authorId", source = "request.author.id")
    @Mapping(target = "request.passengerId", source = "request.passenger.id")
    @Mapping(target = "request.purposeId", source = "request.purpose.id")
    @Mapping(target = "request.tariff", expression = "java(objectToMap(request.getTariff()))")
    @Mapping(target = "request.outcomeTariff", expression = "java(objectToMap(request.getOutcomeTariff()))")
    UpdateTripRequestMessage toMessage(UpdateRequest update);
    
    default UpdateTripRequestMessage toMessage(UpdateRequest update, boolean delete) {
        final UpdateTripRequestMessage result = toMessage(update);
        result.setDeleted(delete);
        return result;
    }
    
    default Map<String, Object> objectToMap(BaseTariff tariff){
        if(tariff != null) {
            var objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.convertValue(tariff, new TypeReference<>() {
            });
        } else return Collections.emptyMap();
    }
    
}
