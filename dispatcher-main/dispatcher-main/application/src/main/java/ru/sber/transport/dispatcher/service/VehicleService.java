package ru.sber.transport.dispatcher.service;

import org.springframework.data.domain.Page;
import ru.sber.transport.dispatcher.database.model.Vehicle;
import ru.sber.transport.dispatcher.dto.*;
import ru.sber.transport.dispatcher.dto.search.TransportSearchDTO;
import ru.sber.transport.dispatcher.dto.search.VehicleSearchDTO;
import ru.sber.transport.dispatcher.dto.search.VehicleShiftSearchDto;
import ru.sber.transport.dispatcher.messages.TransportMessage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for working with Vehicle.
 */
public interface VehicleService {

    /**
     * Сохранение автомобиля
     * @param contractorId ID контрагента
     * @param autoparkId ID автопарка
     * @param vehicleDTO данные автомобиля
     * @return автомобиль
     */
    VehicleDTO add(UUID contractorId, UUID autoparkId, NewVehicleDTO vehicleDTO);

    /**
     * Изменение автомобиля
     * @param contractorId ID контрагента
     * @param autoparkId ID автопарка
     * @param vehicleId ID автомобиля
     * @param vehicleDTO данные автомобиля
     */
    void edit(UUID contractorId, UUID autoparkId, UUID vehicleId, NewVehicleDTO vehicleDTO);

    /**
     * Удаление автомобиля
     * @param contractorId ID контрагента
     * @param autoparkId ID автопарка
     * @param vehicleId ID автомобиля
     */
    void delete(UUID contractorId, UUID autoparkId, UUID vehicleId);

    /**
     * Сохранение или обновление автомобиля
     */
    void upsert(TransportMessage message);

    /**
     * Получение автомобиля по ID
     * @param id ID автомобиля
     * @return автомобиль
     */
    Optional<Vehicle> findById(UUID id);

    /**
     * Получение автомобиля
     * @param contractorId ID контрагента
     * @param autoparkId ID автопарка
     * @param transportId ID автомобиля
     * @return автомобиль
     */
    VehicleDTO get(UUID contractorId, UUID autoparkId, UUID transportId);

    /**
     * Получение автомобилей
     * @param autoparkId ID автопарка
     * @return список автомобилей
     */
    Collection<Vehicle> getAllByAutopark(UUID autoparkId);

    /**
     * Получение активных автомобилей находящихся в эксплуатации
     * @param autoparkId ID автопарка
     * @return список автомобилей
     */
    Collection<Vehicle> getAllByAutoparkAndActiveTrueAndInExploitation(UUID autoparkId);

    /**
     * Получение автомобилей с пагинацией
     * @param contractorId ID контрагента
     * @param autoparkId ID автопарка
     * @param searchDTO данные автомобиля
     * @return страница автомобилей
     */
    Page<VehicleDTO> getAllPageable(UUID contractorId, UUID autoparkId, VehicleSearchDTO searchDTO);

    /**
     * Получение автомобилей по фильтрам
     * @param contractorId ID контрагента
     * @param autoparkId ID автопарка
     * @param searchDTO данные автомобиля
     * @return список автомобилей
     */
    List<VehicleDTO> getAll(UUID contractorId, UUID autoparkId, VehicleSearchDTO searchDTO);

    /**
     * Получение автомобилей по контрагенту и фильтрам
     * @param contractorId ID контрагента
     * @param transportSearchDTO фильтры
     * @return страница автомобилей
     */
    Page<TransportDTO> getAllByContractorAndFilters(UUID contractorId, TransportSearchDTO transportSearchDTO);

    /**
     * Получение свободных автомобилей по контрагенту и фильтрам
     * @param contractorId ID контрагента
     * @param transportSearchDTO фильтры
     * @return страница автомобилей
     */
    Page<TransportDTO> getAllFreeTransport(UUID contractorId, TransportSearchDTO transportSearchDTO);

    /**
     * Получение автомобилей по контрагенту и фильтрам
     * @param contractorId ID контрагента
     * @param vehicleId ID автомобиля
     * @param transportSearchDTO фильтры
     * @return данные о занятости автомобиля
     */
    List<TransportDTO.Trip> getAllByContractorAndVehicleId(UUID contractorId, UUID vehicleId, TransportSearchDTO transportSearchDTO);

    /**
     * Получение автомобилей
     * @param contractorId ID контрагента
     * @param searchDto данные для поиска
     * @return страница автомобилей
     */
    Page<VehicleShiftResponse> getAllByContractorIdAndFilters(UUID contractorId, VehicleShiftSearchDto searchDto);

    /**
     * Получение списка состояний автомобилей на сменах
     * @param contractorId идентификатор контрагента
     * @param vehicleIds список идентификаторов тс
     * @return список состояний автомобилей на сменах
     */
    List<VehicleShiftStatusResponse> getVehicleShiftStatus(UUID contractorId, List<UUID> vehicleIds);
}
