package ru.sberbank.ditsib.transport.request.service;

import ru.sber.transport.tariff.messaging.CarSharingTariffMessage;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingTariff;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с тарифами каршеринга.
 */
public interface CarsharingTariffService {
    
    /**
     * Создать / отредактировать тариф.
     *
     * @param tariff CarsharingTariff.
     *
     * @return сохраненный CarsharingTariff.
     */
    CarsharingTariff save(CarsharingTariff tariff);
    
    /**
     * Сохранить данные тарифа.
     *
     * @param id идентификатор.
     * @param message сообщение.
     */
    void save(UUID id, CarSharingTariffMessage message);
    
    /**
     * Найти тариф по его ID.
     *
     * @param tariffId ID тарифа.
     *
     * @return найденный тариф.
     */
    Optional<CarsharingTariff> getOptionalById(UUID tariffId);
    
    /**
     * Найти тариф по его ID.
     *
     * @param tariffId ID тарифа.
     *
     * @return тариф
     */
    CarsharingTariff getTariffById(UUID tariffId);
    
    /**
     * Удалить тариф каршеринга с поиском его в БД.
     *
     * @param tariffId ID тарифа.
     */
    void deleteById(UUID tariffId);
    
    /**
     * Удалить тариф каршеринга, заранее найденный в БД.
     *
     * @param tariff тариф.
     */
    void delete(CarsharingTariff tariff);
}
