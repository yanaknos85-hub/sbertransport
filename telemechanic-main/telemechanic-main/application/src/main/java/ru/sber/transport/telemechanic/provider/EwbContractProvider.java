package ru.sber.transport.telemechanic.provider;

import ru.sber.transport.telemechanic.messaging.listener.message.EwbContractMessage;

/**
 * Поставщик данных по договорам ЭПЛ
 */
public interface EwbContractProvider {
    
    /**
     * Сохранение
     *
     * @param message {@link EwbContractMessage}
     */
    void save(EwbContractMessage message);
    
}
