package ru.sberbank.ditsib.transport.srm.service;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.srm.model.tariff.TaxiTariff;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис по работе с тарифами такси
 */
public interface TaxiTariffService extends TariffService<TaxiTariff> {

    @Override
    default TransportTypeEnum transportType() {
        return TransportTypeEnum.TAXI;
    }
}
