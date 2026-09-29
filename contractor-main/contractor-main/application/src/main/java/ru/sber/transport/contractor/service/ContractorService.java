package ru.sber.transport.contractor.service;

import ru.sber.transport.contractor.database.model.ContractorType;
import ru.sber.transport.contractor.database.model.ServiceType;
import ru.sber.transport.contractor.dto.search.ContractorSearchDTO;
import ru.sber.transport.contractor.database.model.Contractor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with contractors.
 */
public interface ContractorService {

    /**
     * Проверка существования контрагента.
     *
     * @param name имя контрагента.
     * @param tin ИНН контрагента.
     * @param organizationId идентификатор организации.=
     * @return <code>true</code> если контрагент существует.
     */
    Optional<UUID> isContractorExists(String name, String tin, UUID organizationId);
    
    /**
     * Проверка существования контрагента.
     *
     * @param name имя контрагента.
     * @param tin ИНН контрагента.
     * @param organizationId идентификатор организации.
     * @param exclude исключения из проверки.
     * @return <code>true</code> если контрагент существует.
     */
    Optional<UUID> isContractorExists(String name, String tin, UUID organizationId, Contractor exclude);
    
    /**
     * Save contractor.
     *
     * @param entity contractor.
     * @return saved contractor.
     */
    Contractor save(Contractor entity);
    /**
     * Get contractor by ID.
     *
     * @param id ID of contractor.
     * @return contractor.
     */
    Optional<Contractor> get(UUID id);
    
    /**
     * Удаление контрагента.
     *
     * @param contractor контрагент для удаления.
     * @return удаленный контрагент.
     */
    Contractor delete(Contractor contractor);
    
    /**
     * Получить всех контрагентов.
     *
     * @param paged необходимость в пагинации.
     * @param contractorSearchDTO данные для фильтрации и сортировки.
     * @return коллекция контрагентов.
     */
    Iterable<Contractor> getAll(boolean paged, ContractorSearchDTO contractorSearchDTO, UUID organizationId);

    /**
     * Получить всех котрагентов.
     *
     * @return коллекция еонтрагентов.
     */
    List<Contractor> getAll();

    /**
     * Поиск контрагента по Id
     */
    Optional<Contractor> findById(UUID id);

    /**
     * Cуществует ли контрагент.
     * @param contractorId идентификатор контрагента.
     *
     * @return наличие контрагента.
     */
    boolean isContractorExists(UUID contractorId);

    /**
     * Поиск контрагента по наименованию и ИНН.
     *
     * @param name наименование.
     * @param tin ИНН.
     * @return контрагент.
     */
    Optional<Contractor> findByNameAndTin(String name, String tin);

    /**
     * Получение внутреннего автопарка.
     * @param organizationId идентификатор организации.
     * @param contractor контрагент.
     * @return контрагент.
     */
    Optional<Contractor> getInternalAutoPark(UUID organizationId, Contractor contractor);

    /**
     * Поиск активного контрагента по телефону.
     *
     * @param phone телефон.
     * @return диспетчер.
     */
    Optional<Contractor> findByPhoneAndActive(String phone, boolean active);

    /**
     * Поиск активного контрагента по email.
     *
     * @param email email.
     * @return диспетчер.
     */
    Optional<Contractor> findByEmailAndActive(String email, boolean active);
}
