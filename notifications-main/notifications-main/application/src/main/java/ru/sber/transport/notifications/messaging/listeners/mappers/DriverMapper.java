package ru.sber.transport.notifications.messaging.listeners.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.sber.transport.dispatcher.messages.DriverMessage;
import ru.sber.transport.notifications.database.model.contractor.Driver;

import java.util.Optional;

/**
 * Маппер водителей.
 */
@Mapper
public interface DriverMapper {

    /**
     * Обновить данные.
     *
     * @param target цель.
     * @param source источник.
     */
    @Mapping(target = "phone", source = "contactPhone", qualifiedByName = "phone")
    void update(@MappingTarget Driver target, DriverMessage source);

    @Named("phone")
    default String convertPhone(String source) {
        return Optional.ofNullable(source).map(s -> s.replace("(", "").replace(")", "")).orElse(null);
    }
}
