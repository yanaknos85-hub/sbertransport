package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.TariffMessage;
import ru.sberbank.ditsib.transport.reports.model.tariff.BaseTariff;

import java.util.Optional;
import java.util.UUID;

public interface TariffService<T extends BaseTariff> {
    /**
     * Сохранение тарифа
     *
     * @param tariff тариф
     */
    T save(T tariff);
    
    /**
     * Сохранение тарифа или изменение тарифа по TariffMessage Процедура сохранения тарифа возможна только соответствующим сервисом изза ограничения
     * DiscriminatorColumn у базового абстрактного класса
     *
     * @param message сообщение с данными по тарифу
     */
    T updateOrCreate(TariffMessage message);
    
    /**
     * Поиск тарифа
     *
     * @param id идентификатор тарифа
     */
    Optional<T> findById(UUID id);
    
    /**
     * Перевод в неактивное состояние
     *
     * @param id идентификатор тарифа
     */
    void deactivate(UUID id);
    
    /**
     * Получение типа транспорта с которым работает сервис
     */
    TransportTypeEnum getTransportType();
}
