package ru.sberbank.ditsib.transport.request.service;

import ru.sber.transport.tariff.messaging.PublicTariffMessage;
import ru.sberbank.ditsib.transport.request.database.model.PublicTariff;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с тарифами общественного транспорта.
 */
public interface PublicTariffService {
    
    /**
     * Создать / отредактировать тариф.
     *
     * @param tariff PublicTariff.
     *
     * @return сохраненный PublicTariff.
     */
    PublicTariff save(PublicTariff tariff);
    
    /**
     * Сохранить данные тарифа.
     *
     * @param id идентификатор.
     * @param message сообщение.
     */
    void save(UUID id, PublicTariffMessage message);
    
    /**
     * Найти тариф по его ID.
     *
     * @param tariffId ID тарифа.
     *
     * @return найденный тариф.
     */
    Optional<PublicTariff> getOptionalById(UUID tariffId);
    
    /**
     * Найти тариф по его ID.
     *
     * @param tariffId ID тарифа.
     *
     * @return PublicTariff.
     */
    PublicTariff getTariffById(UUID tariffId);
    
    /**
     * Удалить тариф общественного транспорта с поиском его в БД.
     *
     * @param tariffId ID тарифа.
     */
    void deleteById(UUID tariffId);
    
    /**
     * Удалить тариф общественного транспорта, заранее найденный в БД.
     *
     * @param tariff тариф.
     */
    void delete(PublicTariff tariff);
}
