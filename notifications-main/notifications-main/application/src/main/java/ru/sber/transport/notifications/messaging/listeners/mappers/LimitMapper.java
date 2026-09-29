package ru.sber.transport.notifications.messaging.listeners.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.limits.messaging.LimitMessage;
import ru.sber.transport.notifications.database.model.limits.Limit;

/**
 * Маппер лимитов.
 */
@Mapper
public interface LimitMapper {

    /**
     * Обновить данные.
     *
     * @param target цель.
     * @param source источник.
     */
    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget Limit target, LimitMessage source);

}
