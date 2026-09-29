package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.database.model.FraudType;

import java.util.UUID;

/**
 * Сервис для работы с отправкой фрода в мониторинг
 */
public interface FraudMonitoringService {

    /**
     * Отправить сообщение в топик fraud-monitoring.
     *
     * @param requestId ид запроса
     * @param type      тип фрода
     * @param comment   комментарий к фроду
     */
    void send(UUID requestId, FraudType type, String comment);

}
