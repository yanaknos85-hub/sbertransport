package ru.sber.transport.trip.messaging.providers;

import ru.sber.transport.dispatcher.messages.ContractorMessage;
import ru.sber.transport.trip.database.trips.tables.records.ContractorsRecord;

import java.util.List;
import java.util.UUID;

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
     * Получить информацию о признаке автоназначения контрагента
     *
     * @param contractorId ID контрагента
     * @return признак автоназначения
     */
    boolean isAutoassign(UUID contractorId);

    /**
     * Получить информацию о типе интеграции контрагента
     *
     * @param contractorId ID контрагента
     * @return результат проверки на тип интеграции
     */
    boolean isDispatcher(UUID contractorId);

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
     * Проверка существования контрагента
     *
     * @param contractorId идентификатор контрагента.
     * @return идентификатор контрагента.
     */
    boolean checkContractorExistence(UUID contractorId);

    /**
     * Получить список контрагентов по их идентификаторам.
     * @param contractorIds
     * @return список контрагентов.
     */
    List<ContractorsRecord> getContractorsByIds(List<UUID> contractorIds);
}
