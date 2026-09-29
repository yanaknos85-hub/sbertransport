package ru.sber.transport.contractor.service;

import ru.sber.transport.contractor.dto.ContractorDTO;
import ru.sber.transport.contractor.dto.NewContractorDTO;
import ru.sber.transport.contractor.dto.PatchData;
import ru.sber.transport.contractor.dto.enums.ContractorProjection;
import ru.sber.transport.contractor.dto.search.ContractorSearchDTO;
import ru.sber.transport.contractor.dto.search.TransportSearchDTO;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

/**
 * Controller service for working with contractors.
 */
public interface ContractorControllerService {

    /**
     * Добавление нового контрагента.
     *
     * @param contractor    новый контрагент.
     * @param orgId         идентификатор организации для привязки контрагента.
     * @param authorization токен авторизации
     * @return добавленный контрагент.
     */
    ContractorDTO add(NewContractorDTO contractor, UUID orgId, String authorization);

    /**
     * Редактирование контрагента.
     *
     * @param id            идентификатор изменяемого контрагента.
     * @param contractor    новые данные контрагента.
     * @param orgId         идентификатор организации для привязки контрагента.
     * @param authorization токен авторизации
     */
    void edit(UUID id, NewContractorDTO contractor, UUID orgId, String authorization);

    /**
     * Delete contractor.
     *
     * @param id            ID of contractor to delete.
     * @param authorization токен авторизации
     */
    void delete(UUID id, String authorization);

    /**
     * Get one contractor by its ID.
     *
     * @param id ID of contractor to get data.
     * @return data of contractor.
     */
    ContractorDTO get(UUID id);

    /**
     * Получить всех контрагентов.
     *
     * @param paged               необходимость в пагинации.
     * @param contractorSearchDTO данные для фильтрации и сортировки.
     * @return коллекция контрагентов.
     */
    Iterable<ContractorDTO> get(boolean paged, ContractorSearchDTO contractorSearchDTO, ContractorProjection projection, UUID organizationId);

    /**
     * Получить всех контрагентов.
     *
     * @return коллекция контрагентов.
     */
    Collection<ContractorDTO> getAll();

    /**
     * Редактирование флага автоназначение водителя.
     *
     * @param contractorId contractorId.
     * @param autoassign   flag value.
     */
    void editAutoassignFlag(UUID contractorId, Boolean autoassign);

    /**
     * Поиск свободных автомобилей контрагента
     *
     * @param contractorId       идентификатор контрагента
     * @param transportSearchDTO данные для фильтрации и сортировки
     * @return данные об автомобилях контрагента
     */
    Object getAllFreeTransport(UUID contractorId, TransportSearchDTO transportSearchDTO);

    /**
     * Получение данных о занятости автомобиля
     *
     * @param contractorId       идентификатор контрагента
     * @param vehicleId          идентификатор автомобиля
     * @param transportSearchDTO данные для фильтрации и сортировки
     * @return данные о занятости
     */
    Object getTransport(UUID contractorId, UUID vehicleId, TransportSearchDTO transportSearchDTO);

    /**
     * Редактирование контактного лица.
     * @param contractorId идентификатор контрагента
     * @param data данные о контактном лице
     * @param authorizationHeader заголовок авторизации
     */
    void editContactPerson(UUID contractorId, Serializable data, String authorizationHeader);

    /**
     * Редактирование нормы по количеству автомобилей.
     * @param data данные о норме
     * @param contractorId  идентификатор контрагента
     * @param authorizationHeader заголовок авторизации
     */
    void editVehicleCountNorm(UUID contractorId, PatchData data, String authorizationHeader);
}
