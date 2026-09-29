package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;

import java.util.*;

/**
 * Интерфейс для работы с контрагентами исполнителями поездок на такси
 */
public interface ContractorService {
    
    /**
     * Деактивация контрагента, предварительно найденного в БД
     * @param contractor контрагент
     */
    void softDelete(Contractor contractor);
    
    /**
     * Поиск контрагента по ID
     * @param contractorId ID контрагента
     * @return контрагент
     */
    Optional<Contractor> getOptional(UUID contractorId);
    
    /**
     * Поиск контрагента по ID
     * @param contractorId ID контрагента
     * @return Contractor
     */
    Contractor get(UUID contractorId);

    /**
     * Поиск контрагента по IDs
     * @param contractorIds ID контрагентов
     * @return набор контрагентов
     */
    List<Contractor> getAll(Collection<UUID> contractorIds);

    /**
     * Поиск контрагентов по id заявок
     * @param requestIds ID заявок
     * @return пары Ключ Значение контрагентов, ключ - id контрагента, значение - контрагент
     */
    Map<UUID, Contractor> getAllByRequestIds(Collection<UUID> requestIds);

    /**
     * Сохранить или перезаписать контрагента
     * @param contractor контрагент
     */
    void saveOrUpdate(Contractor contractor);
    
}
