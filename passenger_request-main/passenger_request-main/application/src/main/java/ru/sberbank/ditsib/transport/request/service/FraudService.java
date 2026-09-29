package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.database.model.FraudData;

import java.util.Optional;

/**
 * Сервис для работы с фродом
 */
public interface FraudService {

    /**
     * Сохранение данных о фроде и отправка сообщения в очередь
     * @param fraudData данные о фроде
     */
    void saveAndSend(FraudData fraudData);

    /**
     * Сохранение данных о фроде
     *
     * @param fraudData данные о фроде
     *
     * @return данные о фроде
     */
    Optional<FraudData> save(FraudData fraudData);
}
