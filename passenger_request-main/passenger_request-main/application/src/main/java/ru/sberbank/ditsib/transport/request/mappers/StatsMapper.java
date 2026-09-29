package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.sber.transport.request.model.StatsDTO;
import ru.sberbank.ditsib.transport.request.messaging.message.StatsMessage;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.WARN)
public interface StatsMapper {
    
    @Mapping(target= "id", expression = "java(java.util.UUID.randomUUID())")
    StatsMessage toMessage(StatsDTO statsDTO);
    
}
