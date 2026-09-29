package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.messaging.message.RequestFactDataMessage;
import ru.sberbank.ditsib.transport.request.database.model.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.request.database.model.SingleTaxiTrip;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис взаимодействия с исполнителями поездок на такси
 */
public interface TripService {
    
    /**
     * Получение поездки по id заявки
     * @param requestId id заявки
     * @return поездка
     */
    Optional<SingleTaxiTrip> getSingleTripByRequestId(UUID requestId);
    
    /**
     * Получение поездки по magentaId
     * @param rideId id из системы Мадженты
     * @return поездка
     */
    Optional<CoopTaxiTrip> getCoopTripByRideId(UUID rideId);
    
    /**
     * Обновляет поездки
     * @param taxiTripMessage сообщение
     */
    void updateFactData(RequestFactDataMessage taxiTripMessage);
}
