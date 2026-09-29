package ru.sberbank.ditsib.transport.request.service;

import ru.sber.transport.tariff.messaging.PersonalTariffMessage;
import ru.sberbank.ditsib.transport.request.database.model.personal.PersonalTariff;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с тарифами личного транспорта.
 */
public interface PersonalTariffService {
    
    /**
     * Создать / отредактировать тариф.
     *
     * @param tariff PersonalTariff.
     *
     * @return сохраненный PersonalTariff.
     */
    PersonalTariff save(PersonalTariff tariff);
    
    /**
     * Сохранить данные тарифа.
     *
     * @param id идентификатор.
     * @param message сообщение.
     */
    void save(UUID id, PersonalTariffMessage message);
    
    /**
     * Найти тариф по его ID.
     *
     * @param tariffId ID тарифа.
     *
     * @return найденный тариф.
     */
    Optional<PersonalTariff> getOptionalById(UUID tariffId);
    
    /**
     * Найти тариф по его ID.
     *
     * @param tariffId ID тарифа.
     *
     * @return PersonalTariff.
     */
    PersonalTariff getTariffById(UUID tariffId);
    
    /**
     * Удалить тариф личного транспорта с поиском его в БД.
     *
     * @param tariffId ID тарифа.
     */
    void deleteById(UUID tariffId);
    
    /**
     * Удалить тариф личного транспорта, заранее найденный в БД.
     *
     * @param tariff тариф.
     */
    void delete(PersonalTariff tariff);
}
