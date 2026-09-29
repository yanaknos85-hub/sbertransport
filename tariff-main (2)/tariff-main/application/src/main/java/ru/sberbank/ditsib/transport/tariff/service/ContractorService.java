package ru.sberbank.ditsib.transport.tariff.service;

import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.Contractor;

import java.util.*;

/**
 * Интерфейс для работы с контрагентами.
 */
public interface ContractorService {
    
    /**
     * Удаление контрагента.
     *
     * @param contractor контрагент.
     */
    void delete(Contractor contractor);
    
    /**
     * Получение контрагента.
     *
     * @param id идентификатор контрагента.
     * @return контрагент.
     */
    Optional<Contractor> get(UUID id);
    
    /**
     * Получение контрагента.
     *
     * @param name контрагент.
     * @return контрагент.
     */
    Optional<Contractor> get(String name);
    
    /**
     * Сохранение контрагента.
     *
     * @param contractor контрагент.
     */
    void save(Contractor contractor);
    
    /**
     * Обновление списка регионов контрагента.
     *
     * @param contract договор.
     */
    void updateContractorsRegionIds(Contract contract);
    
    /**
     * Получение имен контрагентов.
     *
     * @param values список идентификаторов.
     * @return сопоставления идентификаторов и именю
     */
    Map<UUID, String> getNames(Collection<UUID> values);
}
