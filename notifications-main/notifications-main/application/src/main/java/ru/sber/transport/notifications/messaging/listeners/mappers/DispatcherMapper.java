package ru.sber.transport.notifications.messaging.listeners.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.sber.transport.dispatcher.messages.ContractorUpdateTripMessage;
import ru.sber.transport.notifications.database.model.contractor.Dispatcher;

import java.util.Optional;

/**
 * Маппере диспетчеров
 */
@Mapper
public interface DispatcherMapper {

    /**
     * Обновить данные.
     *
     * @param target цель.
     * @param source источник.
     */
    @Mapping(target = "phone", source = "phone", qualifiedByName = "phone")
    void update(@MappingTarget Dispatcher target, ContractorUpdateTripMessage.Dispatcher source);

    /**
     * Конвертировать телефон.
     * @param source исходный телефон.
     * @return конвертированный телефон.
     */
    @Named("phone")
    default String convertPhone(String source) {
        return Optional.ofNullable(source).map(s -> s.replace("(", "").replace(")", "")).orElse(null);
    }
}
