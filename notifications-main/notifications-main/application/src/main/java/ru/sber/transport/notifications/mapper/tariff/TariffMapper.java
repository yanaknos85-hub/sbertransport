package ru.sber.transport.notifications.mapper.tariff;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.sber.transport.tariff.messaging.TaxiTariffMessage;
import ru.sber.transport.notifications.database.model.tariff.TaxiTariff;

@Mapper
public interface TariffMapper {

    void update(@MappingTarget TaxiTariff taxiTariff, TaxiTariffMessage message);

}
