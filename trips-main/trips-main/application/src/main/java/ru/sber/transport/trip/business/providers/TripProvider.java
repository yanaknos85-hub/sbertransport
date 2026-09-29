package ru.sber.transport.trip.business.providers;

import ru.sber.transport.trip.business.dto.*;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.database.trips.tables.records.TripsRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер поездок.
 */
public interface TripProvider {

    /**
     * Получить поездку.
     *
     * @param tripId идентификатор поездки.
     * @return поездка.
     */
    Optional<Trip> get(UUID tripId);

    /**
     * Сохранить поездку.
     *
     * @param trip поездка.
     * @return количество сохраненных строк.
     */
    int save(Trip trip);

    /**
     * Получение количества поездок.
     *
     * @param contractorId идентификатор контрагента.
     * @param statuses     список статусов.
     * @return количество поездок.
     */
    int countByContractorIdAndStatusIn(UUID contractorId, List<TripStatus> statuses, UUID autoparkId);

    /**
     * Получить поездку по ID и ID контрагента.
     *
     * @param tripId идентификатор поездки.
     * @param contractorId идентификатор контрагента
     * @return поездка.
     */
    Optional<Trip> findByContractorIdAndId(UUID contractorId, UUID tripId);

    /**
     * Получить поездку по ID и ID контрагента.
     *
     * @param humanReadableId человекочитаемый идентификатор поездки.
     * @param contractorId идентификатор контрагента
     * @return поездка.
     */
    Optional<Trip> findByContractorIdAndHumanReadableId(UUID contractorId, String humanReadableId);

    /**
     * Получить список поездок.
     *
     * @param driverId ID водителя
     * @param tripStatuses список статусов
     * @return список поездок
     */
    List<Trip> findAllByDriverAndStatusIn(UUID driverId, List<TripStatus> tripStatuses);

    /**
     * Получить список поездок.
     *
     * @param contractorId ID контрагента
     * @param searchData список статусов
     * @param statuses список статусов
     * @return список поездок
     */
    Iterable<Trip> findAll(UUID contractorId, RequestSearchDto searchData, List<TripStatus> statuses);

    /**
     * Получить список поездок.
     *
     * @param contractorId ID контрагента.
     * @param dispatcherId ID диспетчера
     * @param statuses     список статусов
     * @param searchData данные для поиска
     * @return список поездок
     */
    Iterable<Trip> findAllByContractorIdAndDispatcherIdAndStatusIn(UUID contractorId, UUID dispatcherId, List<TripStatus> statuses, RequestSearchDto searchData);

    /**
     * Получить список поездок.
     *
     * @param driverId ID водителя
     * @return список поездок
     */
    List<Trip> findAllByDriverIdAndStartTimeAfterAndStatusInOrderByStartTimeDesc(UUID driverId, LocalDateTime startDateTime, List<TripStatus> tripStatuses);

    /**
     * Получить список поездок.
     *
     * @return список поездок
     */
    List<Trip> findTripsForAssigningDriver();

    /**
     * Получить список поездок по ID контрагента.
     *
     * @return список поездок
     */
    List<Trip> findAllByContractorIdOrderByDigitId(UUID contractorId, TripsExportFiltersDTO filters);

    /**
     * Получить список поездок по ID водителя.
     *
     * @return список поездок
     */
    List<Trip> findAllByDriverId(UUID driverId);

    /**
     * Получить страницу поездок.
     *
     * @param driverId ID водителя
     * @param tripStatuses список статусов
     * @return страница поездок
     */
    Iterable<Trip> findAllByDriverAndStatusIn(UUID driverId, List<TripStatus> tripStatuses, RequestSearchDto searchDto);

    /**
     * Получить информацию о занятости водителей.
     *
     * @param driverBusynessRequest уточняющая информация для поиска
     * @param contractorId ID контрагента
     * @param contractorDigitId Digit ID контрагента
     * @return информация о занятости водителей
     */
    List<DriverBusynessDTO.BusynessData> findAllDriverBusyness(DriverBusynessRequest driverBusynessRequest, UUID contractorId, Long contractorDigitId);

    /**
     * Получить информацию о занятости автомобилей.
     *
     * @param vehicleBusynessRequest уточняющая информация для поиска
     * @param contractorId ID контрагента
     * @param contractorDigitId Digit ID контрагента
     * @return информация о занятости автомобилей
     */
    List<VehicleBusynessDTO> findAllVehicleBusyness(VehicleBusynessRequest vehicleBusynessRequest, UUID contractorId, Long contractorDigitId);


    /**
     * Получить информацию о планируемой занятости водителей.
     *
     * @param driverBusynessRequest уточняющая информация для поиска
     * @param contractorId ID контрагента
     * @param contractorDigitId Digit ID контрагента
     * @return информация о планируемой занятости водителей
     */
    List<DriverBusynessDTO.BusynessData> findAllPlanningDriverBusyness(DriverBusynessRequest driverBusynessRequest, UUID contractorId, Long contractorDigitId);

    /**
     * Получить информацию о планируемой занятости автомобилей.
     *
     * @param vehicleBusynessRequest уточняющая информация для поиска
     * @param contractorId ID контрагента
     * @param contractorDigitId Digit ID контрагента
     * @return информация о планируемой занятости автомобилей
     */
    List<VehicleBusynessDTO> findAllPlanningVehicleBusyness(VehicleBusynessRequest vehicleBusynessRequest, UUID contractorId, Long contractorDigitId);

    /**
     * Получить информацию о трипах с забронированными машинами.
     *
     * @param driverBusynessRequest уточняющая информация для поиска
     * @param contractorId ID контрагента
     * @param contractorDigitId Digit ID контрагента
     * @return информация о планируемой занятости водителей
     */
    List<DriverBusynessDTO.TripData> findAllOrderedVehicles(DriverBusynessRequest driverBusynessRequest, UUID contractorId, Long contractorDigitId);

    /**
     * Получить информацию о трипах с забронированными машинами.
     *
     * @param vehicleBusynessRequest уточняющая информация для поиска
     * @param contractorId ID контрагента
     * @param contractorDigitId Digit ID контрагента
     * @return информация о планируемой занятости автомобилей
     */
    List<VehicleBusynessDTO> findAllOrderedVehicles(VehicleBusynessRequest vehicleBusynessRequest, UUID contractorId, Long contractorDigitId);

    /**
     * Получить поездки по ID смены в которую планируется выполнить поездку.
     *
     * @param plannedShiftId ID смены в которую планируется выполнить поездку
     * @return список поездок
     */
    List<Trip> findAllByPlannedShiftId(UUID plannedShiftId);

    /**
     * Получить список поездок по признаку формирования отчета, терминальным статусам с ограничением количества записей.
     * @param limit число записей в ответе
     * @return список поездок
     */
    List<TripsRecord> findAllByReportCreatedFalseAndTerminalStatusesAndLimit(int limit);

    /**
     * Проставить поездкам признак формирования отчета true по списку идентификаторов
     * @param ids список идентификаторов
     */
    void setReportCreatedIsTrueByIds(List<UUID> ids);

    /**
     * Установка фактического пробега и времени ожидания водителя по поездке.
     *
     * @param trip поездка.
     */
    void updateFactData(Trip trip);
}
