package ru.sber.transport.notifications.messaging.listeners.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.sber.transport.limits.messaging.ApproveLimitRequestMessage;
import ru.sber.transport.notifications.database.model.limits.ApproveLimitRequest;

/**
 * Маппер согласований лимитов.
 */
@Mapper
public interface ApproveLimitMapper {

    /**
     * Обновить данные.
     *
     * @param target цель.
     * @param source источник.
     */
    void update(@MappingTarget ApproveLimitRequest target, ApproveLimitRequestMessage source);

}
