package ru.sber.transport.notifications.mapper.trip_request;

import org.mapstruct.Mapper;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.notifications.database.model.request.ExpectedData;

/**
 * Маппер ожидаемых данных поездки.
 */
@Mapper
public interface ExpectedDataMapper {
    
    ExpectedData toEntity(RequestMessage.ExpectedData source);
    
}
