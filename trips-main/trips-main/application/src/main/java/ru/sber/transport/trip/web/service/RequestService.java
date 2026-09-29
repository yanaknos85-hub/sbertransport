package ru.sber.transport.trip.web.service;

import ru.sber.transport.trip.business.dto.FinalInfoTripDto;
import ru.sber.transport.trip.business.dto.RequestSearchDto;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.business.model.TripStatus;

import java.util.List;
import java.util.Map;
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
     * @param withHistory  признак добавляения в ответ исторических данных
     * @return поездка.
     */
    Trip find(UUID contractorId, UUID tripId, boolean withHistory);

    /**
     * Получение поездки.
     *
     * @param contractorId    идентификатор контрагента.
     * @param humanReadableId человекочитаемый идентификатор поездки.
     * @return поездка.
     */
    Optional<Trip> find(UUID contractorId, String humanReadableId);


    /**
     * Запись итоговых данных по поездке.
     *
     * @param contractorId идентификатор контрагента.
     * @param driverId     идентификатор водителя.
     * @param tripId       идентификатор поездки.
     * @param tripDto      данные поездки.
     */
    void updateFinal(UUID contractorId, UUID driverId, UUID tripId, FinalInfoTripDto tripDto);

    /**
     * Запись итоговых данных по поездке.
     *
     * @param tripId        идентификатор поездки.
     * @param factDistances список фактических дистанций поездки.
     */
    void updateFinal(UUID tripId, Map<String, Double> factDistances);

}
