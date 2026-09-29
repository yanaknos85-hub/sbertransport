package ru.sber.transport.notifications.mapper.trip_request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.notifications.database.model.request.Address;

import java.util.Map;

/**
 * Маппер адресов.
 */
@Mapper
public interface AddressMapper {
    
    default Address toEntity(Map<String, Object> source) {
        return objectMapper().convertValue(source, Address.class);
    }

    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }
    
}
