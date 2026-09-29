package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.messaging.messages.DriverMessage;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.reports.dto.DriverDTO;
import ru.sberbank.ditsib.transport.reports.model.driversData.Driver;

import java.util.Optional;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface DriverMapper {
    
    Driver driver(DriverMessage driverMessage);
    
    DriverDTO toDTO(Driver driver);
    
    default String tag(DriverMessage.DriverTag tag) {
        return Optional.ofNullable(tag).map(DriverMessage.DriverTag::getName).orElse(null);
    }
    
    Driver driver(RequestMessage.DriverData driverData);
}
