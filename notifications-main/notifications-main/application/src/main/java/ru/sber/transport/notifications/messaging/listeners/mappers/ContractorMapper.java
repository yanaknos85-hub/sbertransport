package ru.sber.transport.notifications.messaging.listeners.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.dispatcher.messages.ContractorMessage;
import ru.sber.transport.notifications.database.model.contractor.Contractor;

/**
 * Маппер контрагента.
 */
@Mapper
public interface ContractorMapper {

    /**
     * Обновить данные.
     *
     * @param target цель.
     * @param source источник.
     */
    @Mapping(target = "autoparks", ignore = true)
    void update(@MappingTarget Contractor target, ContractorMessage source);
}
