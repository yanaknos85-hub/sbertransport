package ru.sber.transport.trips.cargo.business;

import ru.sber.transport.trips.cargo.business.dto.CreateTripResponse;
import ru.sber.transport.trips.cargo.business.dto.IntegrationRequestDTO;
import ru.sber.transport.trips.cargo.business.model.Trip;

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
     * @param contractorId идентификатор контрагента.
     */
    CreateTripResponse process(IntegrationRequestDTO request, UUID contractorId);

    /**
     * Отменить поездку.
     *
     * @param trip поездка.
     */
    CreateTripResponse processCancel(Trip trip);

}
