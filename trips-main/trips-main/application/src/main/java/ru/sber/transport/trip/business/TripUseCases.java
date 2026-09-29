package ru.sber.transport.trip.business;

import ru.sber.transport.trip.business.dto.CreateTripResponse;
import ru.sber.transport.trip.business.dto.IntegrationRequestDTO;
import ru.sber.transport.trip.business.model.Request;
import ru.sber.transport.trip.business.model.Trip;

import java.util.UUID;

/**
 * Действия с поездками.
 */
public interface TripUseCases<T> {

    /**
     * Обработать заявку.
     *
     * @param request заявка.
     * @param driverId идентификатор водителя.
     * @param vehicleId идентификатор автомобиля.
     */
    void process(T request, UUID driverId, UUID vehicleId);

    /**
     * Обработать заявку.
     *
     * @param request заявка.
     * @param contractorId идентификатор контрагента
     * @return результат обработки заявки.
     */
    CreateTripResponse process(IntegrationRequestDTO request, UUID contractorId);

    /**
     * Обновить статус поездки.
     *
     * @param request заявка.
     * @param trip поездка.
     * @return поездка.
     */
    Trip updateStatus(Trip trip, Request request);

    /**
     * Отменить поездку.
     *
     * @param trip поездка.
     * @return результат обработки заявки.
     */
    CreateTripResponse processCancel(Trip trip);

}
