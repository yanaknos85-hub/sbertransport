package ru.sber.transport.notifications.services;

import ru.sber.transport.tariff.messaging.TariffMessage;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.notifications.database.model.tariff.BaseTariff;

import java.util.Optional;
import java.util.UUID;

public interface TariffService<T extends BaseTariff> {
    /**
     * Сохранение тарифа
     * @param tariff тариф
     */
    T save(T tariff);

    /**
     * Сохранение тарифа или изменение тарифа по TariffMessage
     * Процедура сохранения тарифа возможна только соответствующим сервисом из-за ограничения
     * DiscriminatorColumn у базового абстрактного класса
     * @param id идентификатор тарифа
     * @param message сообщение с данными по тарифу
     */
    T updateOrCreate(UUID id, TariffMessage message);

    /**
     * Поиск тарифа
     * @param id идентификатор тарифа
     */
    Optional<T> findById(UUID id);

    /**
     *  Перевод в неактивное состояние
     * @param id идентификатор тарифа
     */
    void deactivate(UUID id);

    /**
     * Получение типа транспорта с которым работает сервис
     */
    TransportTypeEnum getTransportType();
}
