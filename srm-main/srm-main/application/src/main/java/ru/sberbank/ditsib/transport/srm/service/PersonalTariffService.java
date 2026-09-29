package ru.sberbank.ditsib.transport.srm.service;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.srm.model.tariff.PersonalTariff;

/**
 * Сервис тарифов для личного транспорта
 */
public interface PersonalTariffService extends TariffService<PersonalTariff> {

    @Override
    default TransportTypeEnum transportType() {
        return TransportTypeEnum.PERSONAL;
    }
}
