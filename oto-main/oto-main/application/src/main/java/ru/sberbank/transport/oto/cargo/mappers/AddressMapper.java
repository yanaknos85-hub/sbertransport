package ru.sberbank.transport.oto.cargo.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sberbank.transport.oto.cargo.dto.oto.AddressDto;
import ru.sberbank.transport.oto.cargo.database.model.Address;

import java.util.Map;

@Mapper
public interface AddressMapper {
    
    AddressDto toDto(Address sourceAddress);
    
    default Map<String, Object> toMessage(Address sourceAddress) {
        return objectMapper().convertValue(sourceAddress, new TypeReference<>() {});
    }
    
    default Address toModel(Map<String,Object> sourceAddress) {
        return objectMapper().convertValue(sourceAddress, new TypeReference<>() {});
    }
    
    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }
    
}
