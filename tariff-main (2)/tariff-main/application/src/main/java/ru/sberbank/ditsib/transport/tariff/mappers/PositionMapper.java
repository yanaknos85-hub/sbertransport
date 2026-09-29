package ru.sberbank.ditsib.transport.tariff.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Position;

/**
 * Маппер должностей
 */
@Mapper
public interface PositionMapper {

    /**
     * Преобразование сообщения в модель
     * @param positionMessage сообщение
     * @return модель
     */
    Position toModel(PositionMessage positionMessage);
}
