package ru.sber.transport.trips.cargo.web.service;

import ru.sber.transport.trips.cargo.business.dto.CheckinDTO;
import ru.sber.transport.trips.cargo.business.dto.ShiftOperationType;
import ru.sber.transport.trips.cargo.business.model.*;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для верификации взаимодействия с данными
 */
public interface VerificationService {

    void checkDriverBusynessForStatusChanging(Trip trip, Driver driver);

    void checkEndStatus(Trip trip, TripStatus status);

    void checkDriverToTripRelation(Driver tripDriver, Driver currentDriver);

    void checkDriverToContractorRelation(Driver currentDriver, Trip trip);

    void checkDispatcherToContractorRelation(Dispatcher dispatcher, Trip trip);

    void checkShiftEndDateToTripStartTimeRelation(Shift shift, Trip trip);

    void checkShiftIsDeleted(Shift shift, ShiftOperationType operation);

    void checkDriverToDispatcherRelation(Driver driver, Dispatcher dispatcher);

    void checkDriverBusynessForExitFromShift(Driver driver);

    void checkShiftsExistence(List<Shift> shifts);

    void checkCheckinsExistence(List<CheckinDTO> checkins, long digitId);

    void checkIsTripCancelled(Trip trip, String humanReadableId);

    void checkIsDriverOnline(Driver driver);

    /**
     * Проверка корректности назначения водителя
     * @param patchData тело запроса на изменение поездки
     */
    void checkDriverAssigningCorrectness(Map<String, Serializable> patchData);

    /**
     * Проверка доступности планирования по поездке
     *
     * @param status статус поездки
     */
    void checkPlanningIsAvailable(TripStatus status);

    /**
     * Проверка корректности времени работы грузчиков (0-500 мин)
     * @param loadersWorkTime время работы грузчиков
     */
    void checkLoadersWorkTimeValueRangeCorrectness(Long loadersWorkTime);

    /**
     * Проверка корректности времени ожидания водителя (0-500 мин)
     * @param driverWaitingTime время ожидания водителя
     */
    void checkDriverWaitingTimeValueRangeCorrectness(Duration driverWaitingTime);

    /**
     * Проверка доступности ручного вывода на линию
     * @param shift смена
     */
    void checkManualOnlineSwitchAvailable(Shift shift);
}
