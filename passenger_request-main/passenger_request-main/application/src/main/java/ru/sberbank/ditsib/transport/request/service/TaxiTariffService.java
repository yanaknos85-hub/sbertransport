package ru.sberbank.ditsib.transport.request.service;

import ru.sber.transport.tariff.messaging.TaxiTariffMessage;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с тарифами такси.
 */
public interface TaxiTariffService {
    
    /**
     * Создать / отредактировать тариф.
     *
     * @param tariff TaxiTariff.
     *
     * @return сохраненный TaxiTariff.
     */
    TaxiTariff save(TaxiTariff tariff);
    
    /**
     * Сохранить данные тарифа.
     *
     * @param id идентификатор.
     * @param message сообщение.
     */
    void save(UUID id, TaxiTariffMessage message);
    
    /**
     * Найти тариф по его ID.
     *
     * @param tariffId ID тарифа.
     *
     * @return тариф
     */
    Optional<TaxiTariff> getOptionalById(UUID tariffId);
    
    /**
     * Найти тариф по его ID.
     *
     * @param tariffId ID тарифа.
     *
     * @return TaxiTariff.
     */
    TaxiTariff getTariffById(UUID tariffId);
    
    /**
     * Удалить тариф такси с поиском его в БД.
     *
     * @param tariffId ID тарифа.
     */
    void deleteById(UUID tariffId);
    
    /**
     * Удалить тариф такси, заранее найденный в БД.
     *
     * @param tariff тариф.
     */
    void delete(TaxiTariff tariff);
}
