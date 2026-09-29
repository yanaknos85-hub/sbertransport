package ru.sber.transport.notifications.messaging.listeners.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.notifications.database.model.request.Driver;
import ru.sber.transport.request.messaging.TaxiTripMessage;
import ru.sber.transport.notifications.database.model.request.TaxiTrip;

/**
 * Маппер поездок.
 */
@Mapper(uses = VehicleMessageMapper.class)
public interface TaxiTripMapper {

    /**
     * Обновить данные.
     *
     * @param target цель.
     * @param source источник.
     */
    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget TaxiTrip target, TaxiTripMessage source);

    Driver update(TaxiTripMessage.Driver source);

}
