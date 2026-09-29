package ru.sber.transport.trips.cargo.web.service;

import ru.sber.transport.trips.cargo.business.dto.EditTripDto;
import ru.sber.transport.trips.cargo.business.dto.FinalInfoTripDto;
import ru.sber.transport.trips.cargo.business.dto.RequestSearchDto;
import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.business.model.TripStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис по работе с заявками.
 */
public interface RequestService {

    /**
     * Найти водителей.
     *
     * @param contractorId ID of contractor.
     * @param dispatcherId идентификатор диспетчера.
     * @param statuses     статусы.
     * @param searchDto    driver data.
     * @param tripType     тип поездки.
     * @return found drivers.
     */
    Iterable<Trip> find(UUID contractorId, UUID dispatcherId, List<TripStatus> statuses, RequestSearchDto searchDto) throws NoSuchFieldException;

    /**
     * Найти поездки водителей.
     *
     * @param driverId  ID of driver.
     * @param searchDto driver data.
     * @return found drivers.
     */
    Iterable<Trip> findByDriver(UUID driverId, List<TripStatus> statuses, RequestSearchDto searchDto) throws NoSuchFieldException;

    /**
     * Найти заявку.
     *
     * @param requestId ID of requestId.
     * @return request data.
     */
    Optional<Trip> get(UUID requestId);

    /**
     * Получение поездки.
     *
     * @param contractorId идентификатор контрагента.
     * @param tripId       идентификатор поездки.
     * @return поездка.
     */
    Trip find(UUID contractorId, UUID tripId);

    /**
     * Получение поездки.
     *
     * @param contractorId    идентификатор контрагента.
     * @param humanReadableId человекочитаемый идентификатор поездки.
     * @return поездка.
     */
    Optional<Trip> find(UUID contractorId, String humanReadableId);

    /**
     * Обновление поездки.
     *
     * @param contractorId идентификатор контрагента.
     * @param dispatcherId идентификатор диспетчера.
     * @param tripId       идентификатор поездки.
     * @param tripDto      данные поездки.
     */
    void update(UUID contractorId, UUID dispatcherId, UUID tripId, EditTripDto tripDto);


    /**
     * Запись итоговых данных по поездке.
     *
     * @param contractorId идентификатор контрагента.
     * @param driverId     идентификатор водителя.
     * @param tripId       идентификатор поездки.
     * @param tripDto      данные поездки.
     */
    void updateFinal(UUID contractorId, UUID driverId, UUID tripId, FinalInfoTripDto tripDto);

}
