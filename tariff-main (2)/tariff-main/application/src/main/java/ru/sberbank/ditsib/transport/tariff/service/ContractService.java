package ru.sberbank.ditsib.transport.tariff.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.ContractType;
import ru.sberbank.ditsib.transport.tariff.dto.ContractSearchDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with Contracts
 */
public interface ContractService {
    
    /**
     * Add Contract
     * @param contract limit Contract
     */
    Contract add(Contract contract);
    
    /**
     * Save Contract
     * @param contract Contract
     */
    Contract save(Contract contract);
    
    /**
     * Delete Contract.
     * @param contract Contract.
     */
    void delete(Contract contract);
    
    /**
     * Get Contract.
     * @param id ID of Contract.
     * @return Contract.
     */
    Contract get(UUID id);
    
    /**
     * Get all Contracts
     * @return list of Contracts
     */
    List<Contract> getAll();
    
    /**
     * Get all Contracts
     *
     * @param contractType тип договора
     *
     * @return list of Contracts
     */
    List<Contract> getAllByContractType(ContractType contractType);
    
    /**
     * Get all Contracts by
     * @param contractorId
     * @return list of Contracts
     */
    List<Contract> getByContractor(UUID contractorId);
    
    /**
     * Get all unique uvhd of contractor.
     *
     * @param contractorId id of contractor.
     * @param uvhd substring uvhd of contract.
     * @param pageable pagination.
     *
     * @return page of uvhd.
     */
    Page<String> getUniqueUvhd(UUID contractorId, String uvhd, TransportTypeEnum transportType, Pageable pageable);
    
    /**
     * Получить список контрактов по фильтру
     * @param searchDTO поисковый  dto
     * @param pageable параметры пейджинга
     * @return список найденных контрактов
     */
    Page<Contract> search(ContractSearchDTO searchDTO, Pageable pageable);
    
    /**
     * Получение договора контрагента по номеру договора.
     *
     * @param contractorId идентификатор контрагентов.
     * @param contractNumber номер договора.
     * @return договор.
     */
    Optional<Contract> get(UUID contractorId, String contractNumber);
    
    /**
     * Получение договора контрагента по номеру договора.
     *
     * @param contractor контрагент.
     * @param contractNumber номер договора.
     * @return договор.
     */
    Optional<Contract> get(String contractor, String contractNumber);
    
    List<Contract> get(List<UUID> toList);
    
    /**
     * Повторная отправка всех контрактов по кафке
     */
    void resend();
    
    /**
     * Метод помечающий контракты не активными по истечению срока
     */
    void checkExpiredContracts();
}
