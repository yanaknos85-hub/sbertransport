package ru.sber.transport.trip.web.service;

import ru.sber.transport.trip.business.dto.CheckinDTO;
import ru.sber.transport.trip.business.dto.ShiftOperationType;
import ru.sber.transport.trip.business.model.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Сервис для верификации взаимодействия с данными
 */
public interface VerificationService {

    /**
     * Проверка занятости водителя для смены статуса поездки
     *
     * @param trip   поездка
     * @param driver водитель
     */
    void checkDriverBusynessForStatusChanging(Trip trip, Driver driver);

    /**
     * Проверка конечного статуса
     *
     * @param trip   поездка
     * @param status статус
     */
    void checkEndStatus(Trip trip, TripStatus status);

    /**
     * Проверка принадлежности водителя к поездке
     *
     * @param tripDriver    водитель поездки
     * @param currentDriver текущий водитель
     */
    void checkDriverToTripRelation(Driver tripDriver, Driver currentDriver);

    /**
     * Проверка принадлежности водителя к контрагенту поездки
     *
     * @param currentDriver текущий водитель
     * @param trip          поездка
     */
    void checkDriverToContractorRelation(Driver currentDriver, Trip trip);

    /**
     * Проверка принадлежности диспетчера к контрагенту поездки
     *
     * @param dispatcher диспетчер
     * @param trip       поездка
     */
    void checkDispatcherToContractorRelation(Dispatcher dispatcher, Trip trip);

    /**
     * Проверка отношения даты окончания смены к времени начала поездки
     *
     * @param shift смена
     * @param trip  поездка
     */
    void checkShiftEndDateToTripStartTimeRelation(Shift shift, Trip trip);

    /**
     * Проверка удаления смены
     *
     * @param shift     смена
     * @param operation операция
     */
    void checkShiftIsDeleted(Shift shift, ShiftOperationType operation);

    /**
     * Проверка принадлежности водителя и диспетчера к одному контрагенту
     *
     * @param driver     водитель
     * @param dispatcher диспетчер
     */
    void checkDriverToDispatcherRelation(Driver driver, Dispatcher dispatcher);

    /**
     * Проверка занятости водителя для выхода со смены
     *
     * @param driver водитель
     */
    void checkDriverBusynessForExitFromShift(Driver driver);

    /**
     * Проверка существования смены
     *
     * @param shifts смена
     */
    void checkShiftsExistence(List<Shift> shifts);

    /**
     * Проверка существования чекинов
     *
     * @param size    размер
     * @param digitId порядковый номер поездки контрагента
     */
    void checkCheckinsExistence(int size, long digitId);

    /**
     * Проверка отмененности поездки
     *
     * @param trip            поездка
     * @param humanReadableId человекочитаемый идентификатор поездки
     */
    void checkIsTripCancelled(Trip trip, String humanReadableId);

    /**
     * Проверка выхода на смену водителем
     *
     * @param driver водитель
     */
    void checkIsDriverOnline(Driver driver);

    /**
     * Проверка статуса на невозможность его примменения в конкретной ситуации
     *
     * @param impossibleStatuses список невозможных статусов
     * @param tripStatus         статус
     */
    void checkImpossibleStatus(List<TripStatus> impossibleStatuses, TripStatus tripStatus);

    /**
     * Проверка статуса на возможность его применения в конкретной ситуации.
     *
     * @param possibleStatuses список возможных статусов.
     * @param tripStatus       назначаемый статус.
     */
    void checkPossibleStatus(List<TripStatus> possibleStatuses, TripStatus tripStatus);

    /**
     * Проверка корректности назначения водителя
     *
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
     * Проверка доступности переназначения водителя.
     *
     * @param trip поездка.
     */
    void checkDriverSwitchIsAvailable(Trip trip);

    /**
     * Проверка доступности ручного вывода на линию
     * @param shift смена
     */
    void checkManualOnlineSwitchAvailable(Shift shift);

}
