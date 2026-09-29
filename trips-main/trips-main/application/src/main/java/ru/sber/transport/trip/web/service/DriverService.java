package ru.sber.transport.trip.web.service;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import ru.sber.transport.trip.business.dto.*;
import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.business.model.Trip;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DriverService {

    /**
     * Проверка поездки водителя
     *
     * @param tripId ID поездки
     * @param driver водитель
     */
    void checkCurrentTrip(UUID tripId, Driver driver);

    /**
     * Информация о последней точке.
     *
     * @param geoWaypointDTO geo info.
     * @param driver driver.
     *
     * @return success.
     */
    void lastPointInfo(GeoWaypointDTO geoWaypointDTO, Driver driver);

    /**
     * Начать или закончить рабочий день - выйти на линию.
     *
     * @param state state.
     * @param driver driver.
     */
    void setOnline(Driver driver, boolean state);

    /**
     * Получение чек-инов по поездке
     *
     * @param driver водитель
     * @param tripId ID поездки
     * @return информация о чек-инах поездки
     * @deprecated функционал универсализирован и перенесен в TripService.getTripCheckins()
     */
    @Deprecated(forRemoval = true)
    CheckinResponseDTO getTripCheckins(Driver driver, UUID tripId);

    /**
     * Получение списка водителей и автомобилей по координатам
     * @param contractorId ID контрагента
     * @param driverLocationSearchDTO данные для поиска водителей
     * @param authentication объект аутентификации
     * @return список водителей и автомобилей
     */
    Page<DriverShiftDTO> getDriversByCoordinates(UUID contractorId, DriverLocationSearchDTO driverLocationSearchDTO, Authentication authentication);

    /**
     * Автоназначение водителя.
     *
     * @param trip заявка.
     *
     * @return driver.
     */
    Driver assignDriver(Trip trip);

    /**
     * Получение информации о занятости водителей.
     * @param authentication данные об авторизованном пользователе.
     * @param driverBusynessRequest уточнющие данные для поиска.
     * @return инвормация о занятости водителей.
     */
    DriverBusynessDTO getDriverBusyness(Authentication authentication, DriverBusynessRequest driverBusynessRequest);

    /**
     * @deprecated
     * Освобождение водителей от зависших поездок (Предназначено только до момента окончательной локализации дефекта).
     */
    void liberateDrivers();
}
