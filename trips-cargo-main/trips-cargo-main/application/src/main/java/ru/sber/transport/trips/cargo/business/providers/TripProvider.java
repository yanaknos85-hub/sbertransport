package ru.sber.transport.trips.cargo.business.providers;

import org.springframework.data.domain.Pageable;
import ru.sber.transport.trips.cargo.business.dto.*;
import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.business.model.TripStatus;

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
     * @param contractorId идентификатор автопарка.
     * @param statuses     список статусов.
     * @return количество поездок.
     */
    int countByContractorIdAndStatusIn(UUID contractorId, UUID autoparkId, List<TripStatus> statuses);

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
    Iterable<Trip> findAll(UUID contractorId, RequestSearchDto searchData, List<TripStatus> statuses, Pageable pageable);

    /**
     * Получить список поездок.
     *
     * @param contractorId ID контрагента.
     * @param dispatcherId ID диспетчера
     * @param statuses     список статусов
     * @param searchData данные для поиска.
     * @return список поездок
     */
    Iterable<Trip> findAllByContractorIdAndDispatcherIdAndStatusIn(UUID contractorId, UUID dispatcherId, List<TripStatus> statuses, Pageable pageable, RequestSearchDto searchData);

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

    List<VehicleBusynessDTO> findAllVehicleBusyness(VehicleBusynessRequest vehicleBusynessRequest, UUID contractorId, Long contractorDigitId);

    /**
     * Получить поездку.
     *
     * @param routeHumanReadableId человекочитаемый идентификатор поездки.
     * @return поездка.
     */
    Optional<Trip> getByRouteHumanReadableId(String routeHumanReadableId);

    /**
     * Получить поездки по ID смены в которую планируется выполнить поездку.
     *
     * @param plannedShiftId ID смены в которую планируется выполнить поездку
     * @return список поездок
     */
    List<Trip> findAllByPlannedShiftId(UUID plannedShiftId);
}
