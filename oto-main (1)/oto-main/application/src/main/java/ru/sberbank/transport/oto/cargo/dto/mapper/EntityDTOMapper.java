package ru.sberbank.transport.oto.cargo.dto.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sberbank.ditsib.transport.request.messaging.AddressMessage;
import ru.sberbank.transport.oto.cargo.database.model.Address;

import java.util.Map;

@Mapper
public interface EntityDTOMapper {
   
    default Address addressMessageToAddress(Map<String, Object> addressMessage){
        return objectMapper().convertValue(addressMessage, new TypeReference<>() {});
    }
    
    @Mapping(source = "addressStringRepresentation", target = "addressString")
    Address addressMessageToAddress(AddressMessage addressMessage);
            @Lookup
    default ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
