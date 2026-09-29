package ru.sber.transport.trips.cargo.messaging.providers;

import ru.sber.transport.dispatcher.messages.ContractorMessage;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * Провайдер контрагентов.
 */
public interface ContractorProvider {

    /**
     * Сохранить.
     *
     * @param id      идентификатор контрагента.
     * @param message сообщение с данными.
     * @return
     */
    int save(UUID id, ContractorMessage message);

    /**
     * Получить последний порядковый номер.
     *
     * @param contractorId идентификатор контрагента.
     * @return последний идентификатор.
     */
    long nextDigit(UUID contractorId);

    /**
     * Получить порядковый номер контрагента.
     *
     * @param contractorId идентификатор контрагента.
     * @return идентификатор контрагента.
     */
    long getContractorDigitId(UUID contractorId);

    /**
     * Получить информацию о признаке автоназначения контрагента
     *
     * @param contractorId ID контрагента
     * @return признак автоназначения
     */
    boolean isAutoassign(UUID contractorId);

    /**
     * Проверка существования контрагента
     *
     * @param contractorId идентификатор контрагента.
     * @return идентификатор контрагента.
     */
    boolean checkContractorExistence(UUID contractorId);
}
