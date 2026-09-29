package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.request.database.model.ExpectedData;

/**
 * Маппер ожидаемых данных.
 */
@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ExpectedDataMapper {
    
    @Mapping(target = "cost", source = "cost")
    @Mapping(target = "distance", source = "distance")
    @Mapping(target = "time", source = "time")
    RequestMessage.ExpectedData toMessage(ExpectedData data);
    
}
