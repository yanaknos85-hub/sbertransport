package ru.sber.transport.dispatcher.service;

import org.springframework.data.domain.Page;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.dto.LinkRequestDTO;
import ru.sber.transport.dispatcher.dto.search.ContractorSearchDTO;

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
     * @param tin  ИНН
     * @return <code>true</code> если контрагент существует.
     */
    Optional<Contractor> isContractorExists(String name, String tin);

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
     * @param contractorSearchDTO данные для фильтрации и сортировки.
     * @return коллекция контрагентов.
     */
    Page<Contractor> getAll(ContractorSearchDTO contractorSearchDTO);

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
     *
     * @param contractorId идентификатор контрагента.
     * @return наличие контрагента.
     */
    boolean isContractorExists(UUID contractorId);

    /**
     * Поиск подходящего контрагента для связывания с внешним инстансом по ОГРН, ИНН, почте
     *
     * @param requestDto данные для связывания
     */
    Contractor findForLink(LinkRequestDTO requestDto);

}
