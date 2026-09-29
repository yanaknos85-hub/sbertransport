package ru.sberbank.transport.oto.cargo.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.route.messaging.RouteMessage;
import ru.sberbank.transport.oto.cargo.database.model.Routelist;

@Mapper
public interface RouteMapper {
    
    Routelist messageToEntity(RouteMessage message);
    
}
