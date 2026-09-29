package ru.sber.transport.telemechanic.provider;

import ru.sber.transport.telemechanic.messaging.listener.message.EwbTariffMessage;

/**
 * Поставщик данных по тарифам ЭПЛ
 */
public interface EwbTariffProvider {
    
    /**
     * Сохранение
     *
     * @param message {@link EwbTariffMessage}
     */
    void save(EwbTariffMessage message);
    
}
