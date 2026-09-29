package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.messaging.messages.RequestStatusOverdueMessage;
import ru.sberbank.ditsib.transport.reports.model.RequestStatusOverdue;

/**
 * Маппер записей о просроченной по КС заявке
 */
@Mapper
public interface RequestStatusOverdueMapper {
    
    RequestStatusOverdue toModel(RequestStatusOverdueMessage model);
    
}
