package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.sberbank.ditsib.transport.messaging.messages.AutoparkMessage;
import ru.sberbank.ditsib.transport.reports.model.driversData.Autopark;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AutoparkDTOMapper {
    
    Autopark autoparkMessageToAutopark(AutoparkMessage autoparkMessage);
    
}
