package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.messaging.messages.TripPurposeMessage;
import ru.sberbank.ditsib.transport.reports.model.TripPurpose;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TripPurposeMapper {
   
    TripPurposeMessage toMessage(TripPurpose tripPurpose);
}
