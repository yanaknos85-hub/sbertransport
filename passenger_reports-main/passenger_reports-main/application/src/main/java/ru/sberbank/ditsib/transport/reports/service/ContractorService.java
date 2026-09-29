package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.model.Contractor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис по операциям с контрагентами/каршеринговыми компаниями
 */
public interface ContractorService {
    /**
     * Поиск контрагента по ID
     * @param id
     * @return контрагент
     */
    Optional<Contractor> findById(UUID id);
    
    /**
     * Удаление контрагента
     * @param contractor
     */
    void delete(Contractor contractor);
    
    /**
     * Сохранение контрагента
     * @param contractor
     * @return
     */
    Contractor save(Contractor contractor);
    
    /**
     * Поиск контрагента по id, создание нового при невозможности найти
     * @param contractorId
     * @return
     */
    Contractor findOrCreateContractorById(UUID contractorId);
    
    List<Contractor> findAll();
}
