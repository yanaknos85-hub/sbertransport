package ru.sberbank.transport.oto.cargo.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.transport.oto.cargo.database.model.drivers_data.Driver;
import ru.sberbank.transport.oto.cargo.messaging.messages.RequestMessage;

@Mapper
public interface DriverMapper {
    
    @Mapping(target = "contactPhone", source = "phoneNumber")
    Driver driver(RequestMessage.DriverData driverMessage);
    
}
