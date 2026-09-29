package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.request.TaxiTrip;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для получения данных по совместным и индивидуальным поездкам
 */
public interface TaxiTripService {
    
    /**
     * Получение данных поездки на такси по id заявки
     * @param requestId id заявки
     * @return данные поездки на такси
     */
    TaxiTrip getByRequestId(UUID requestId);
    
    /**
     * Получение данных поездки на такси по id совместной поездки
     * @param sharedRideId
     * @return данные поездки на такси
     */
    TaxiTrip getBySharedRideId(UUID sharedRideId);
    
    TaxiTrip save(TaxiTrip taxiTrip);

    Optional<TaxiTrip> get(UUID id);
}
