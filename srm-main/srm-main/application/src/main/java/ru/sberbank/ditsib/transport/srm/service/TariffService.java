package ru.sberbank.ditsib.transport.srm.service;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.srm.model.tariff.BaseTariff;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис тарифов
 *
 * @param <T> тип тарифа
 */
public interface TariffService<T extends BaseTariff> {

    /**
     * Сохранить тариф
     * @param tariff тариф
     * @return сохраненный тариф
     */
    T save(T tariff);

    /**
     * Удалить тариф
     * @param tariff тариф
     */
    void delete(T tariff);

    /**
     * Получить тип транспорта
     * @return тип
     */
    TransportTypeEnum transportType();

    /**
     * Найти по ID
     * @param tariffId ID
     * @return тариф
     */
    Optional<T> findById(UUID tariffId);
}
