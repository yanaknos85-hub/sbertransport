package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.request.messaging.message.LimitMessage;
import ru.sberbank.ditsib.transport.request.database.model.DepLimit;

@Mapper
public interface DepLimitMapper {
    
    DepLimit toModel(LimitMessage message);
}
