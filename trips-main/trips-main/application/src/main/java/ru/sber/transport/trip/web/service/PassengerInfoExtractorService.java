package ru.sber.transport.trip.web.service;

import ru.sber.transport.trip.business.model.Request;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.web.dto.PassengersTripDTO;

import java.util.List;

/**
 * Сервис для извлечения информации о пассажирах из запроса или поездки.
 */
public interface PassengerInfoExtractorService {

    /**
     * Извлечь данных о пассаджирах
     * @param requests - список запросов, в которых может содержаться информация о пассажирах
     * @param item - поездка, в которой может содержаться информация о пассажирах
     * @return - данные о пассажирах
     */
    PassengersTripDTO extractPassengersInfo(List<Request> requests, Trip item);
}
