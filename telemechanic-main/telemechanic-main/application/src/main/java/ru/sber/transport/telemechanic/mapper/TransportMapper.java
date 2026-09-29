package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.telemechanic.database.model.Transport;
import ru.sber.transport.telemechanic.dto.transport.GetTransportResponse;

@Mapper(componentModel = "spring")
public interface TransportMapper {
    
    @Mapping(target = "transportType", source = "type")
    GetTransportResponse transportToGetTransportResponse(Transport source);
    
}
