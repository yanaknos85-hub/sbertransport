package ru.sberbank.transport.oto.cargo.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.transport.oto.cargo.database.model.RequestStatusOverdue;
import ru.sberbank.transport.oto.cargo.messaging.messages.RequestStatusOverdueMessage;

/**
 * Маппер записей о просроченной по КС заявке
 */
@Mapper
public interface RequestStatusOverdueMapper {
    
    RequestStatusOverdue toModel(RequestStatusOverdueMessage model);
    
}
