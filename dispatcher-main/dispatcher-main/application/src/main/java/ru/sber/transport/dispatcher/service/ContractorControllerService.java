package ru.sber.transport.dispatcher.service;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import ru.sber.transport.dispatcher.dto.*;
import ru.sber.transport.dispatcher.dto.search.ContractorSearchDTO;
import ru.sber.transport.dispatcher.dto.search.TransportSearchDTO;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Controller service for working with contractors.
 */
public interface ContractorControllerService {

    /**
     * Добавление нового контрагента.
     *
     * @param contractor новый контрагент.
     * @return добавленный контрагент.
     */
    ContractorDTO add(NewContractorDTO contractor);

    /**
     * Редактирование контрагента.
     *
     * @param id         идентификатор изменяемого контрагента.
     * @param contractor новые данные контрагента.
     */
    void edit(UUID id, NewContractorDTO contractor);

    /**
     * Delete contractor.
     *
     * @param id ID of contractor to delete.
     */
    void delete(UUID id);

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
     * @param contractorSearchDTO данные для фильтрации и сортировки.
     * @return коллекция контрагентов.
     */
    Page<ContractorDTO> get(ContractorSearchDTO contractorSearchDTO);

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
     * Редактирование нормы по количеству автомобилей.
     * @param contractorId идентификатор контрагента
     * @param vehicleCountNorm норма по количеству автомобилей
     */
    void editVehicleCountNorm(UUID contractorId, Integer vehicleCountNorm);

    /**
     * Назначить главного диспетчера.
     * @param contractorId контрагент
     * @param dispatcherId диспетчер
     */
    void setMainDispatcher(UUID contractorId, UUID dispatcherId);

    /**
     * Поиск свободных автомобилей контрагента
     *
     * @param transportSearchDTO данные для поиска
     * @param contractorId       идентификатор контрагента
     * @return данные об автомобилях контрагента
     */
    ru.sber.transport.dto.Page<TransportDTO> getAllFreeTransport(UUID contractorId, TransportSearchDTO transportSearchDTO);

    /**
     * Поиск свободных автомобилей контрагента
     *
     * @param transportSearchDTO данные для поиска
     * @param contractorId       идентификатор контрагента
     * @return данные об автомобилях контрагента
     */
    Page<TransportDTO> getAllFreeTransportV2(UUID contractorId, TransportSearchDTO transportSearchDTO);

    /**
     * Получение занятых слотов автомобиля
     *
     * @param vehicleId          идентификатор автомобиля
     * @param contractorId       идентификатор контрагента
     * @param transportSearchDTO данные для поиска
     * @return данные о занятости автомобиля
     */
    List<TransportDTO.Trip> getTransport(UUID contractorId, UUID vehicleId, TransportSearchDTO transportSearchDTO);

    /**
     * Связывание контрагентов в разных инстансах по ОГРН, ИНН, почте
     *
     * @param requestDto данные для связывания
     */
    void linkContractor(LinkRequestDTO requestDto);

    /**
     * Получение нормативных показателей по филиалам автопарка
     *
     * @param contractorId идентификатор контрагента
     * @return нормативные показатели
     */
    VehicleNormDto getVehicleNorm(@NonNull UUID contractorId);
}
