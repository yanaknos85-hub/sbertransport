package ru.sber.transport.trip.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.trip.business.dto.ShiftOperationType;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.web.exceptions.ConflictException;
import ru.sber.transport.trip.web.service.VerificationService;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
class VerificationServiceImpl implements VerificationService {

    private static final String CAN_NOT_CHANGE_STATUS_STRING = "Нельзя изменить статус поездки на ";

    @Override
    public void checkDriverBusynessForStatusChanging(Trip trip, Driver driver) {
        if (driver.getActiveTripId() != null && !driver.getActiveTripId().equals(trip.getId())) {
            throw new ConflictException("В данный момент выполняется другой заказ, сменить статус нельзя");
        }
    }

    @Override
    public void checkEndStatus(Trip trip, TripStatus status) {
        if (trip.getStatus().isTerminal()) {
            throw new ConflictException(CAN_NOT_CHANGE_STATUS_STRING + status + ", так как поездка находится в конечном статусе ");
        }
    }

    @Override
    public void checkDriverToTripRelation(Driver tripDriver, Driver currentDriver) {
        if (tripDriver == null || !currentDriver.getId().equals(tripDriver.getId())) {
            throw new ConflictException("Поездка не назначена на текущего водителя");
        }
    }

    @Override
    public void checkDriverToContractorRelation(Driver currentDriver, Trip trip) {
        if (!currentDriver.getContractorId().equals(trip.getContractorId())) {
            throw new ConflictException("Водитель не приндалежит контрагенту поездки");
        }
    }

    @Override
    public void checkDispatcherToContractorRelation(Dispatcher dispatcher, Trip trip) {
        if (dispatcher.getContractorId() != trip.getContractorId()) {
            throw new ConflictException("Поездка не принадлежит контрагенту диспетчера");
        }
    }

    @Override
    public void checkShiftEndDateToTripStartTimeRelation(Shift shift, Trip trip) {
        if (trip.getExpectedStartTime().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime().isAfter(shift.getEndDate())) {
            throw new ConflictException("Нельзя назначить водителя на данную поездку, смена водителя заканчивается раньше ожидаемого начала выполнения заказа");
        }
    }

    @Override
    public void checkShiftIsDeleted(Shift shift, ShiftOperationType operation) {
        if (shift.isDeleted()) {
            switch (operation) {
                case ACTIVATION, DEACTIVATION ->
                        throw new ConflictException("Смена удалена, " + operation.getName() + " невозможна");
                case EXIT -> throw new ConflictException("Не удалось уйти с линии, смена водителя удалена");
                case ENTER -> throw new ConflictException("Смена водителя удалена, выйти на линию нельзя");
                case TRIP_PLANNING ->
                        throw new ConflictException("Смена водителя удалена, запланировать на нее поездку нельзя");
            }
        }
    }

    @Override
    public void checkDriverToDispatcherRelation(Driver driver, Dispatcher dispatcher) {
        if (!driver.getContractorId().equals(dispatcher.getContractorId())) {
            throw new ConflictException("Водитель не принадлежит контрагенту диспетчера");
        }
    }

    @Override
    public void checkDriverBusynessForExitFromShift(Driver driver) {
        if (driver.isServing() || driver.getActiveTripId() != null) {
            throw new ConflictException("Не удалось уйти с линии, у водителя есть выполнямые заказы");
        }
    }

    @Override
    public void checkShiftsExistence(List<Shift> shifts) {
        if (shifts.isEmpty()) {
            throw new ConflictException("У водителя нет смен на сегодняшний день или смена еще не началась");
        }
    }

    @Override
    public void checkCheckinsExistence(int size, long digitId) {
        if (size == 0) {
            throw new ConflictException("По поездке " + digitId + " отсутствуют чек-ины");
        }
    }

    @Override
    public void checkIsTripCancelled(Trip trip, String humanReadableId) {
        if (!TripStatus.ORDER_FINISHED.equals(trip.getStatus()) && trip.getStatus().isTerminal()) {
            throw new ConflictException("Поездка " + humanReadableId + " отменена");
        }
    }

    @Override
    public void checkIsDriverOnline(Driver driver) {
        if (!driver.isOnline()) {
            throw new ConflictException("Водитель не находится на линии");
        }
    }

    @Override
    public void checkImpossibleStatus(List<TripStatus> impossibleStatuses, TripStatus tripStatus) {
        if (impossibleStatuses.contains(tripStatus)) {
            throw new ConflictException(CAN_NOT_CHANGE_STATUS_STRING + tripStatus);
        }
    }

    @Override
    public void checkPossibleStatus(List<TripStatus> possibleStatuses, TripStatus tripStatus) {
        if (!possibleStatuses.contains(tripStatus)) {
            throw new ConflictException(CAN_NOT_CHANGE_STATUS_STRING + tripStatus);
        }
    }

    @Override
    public void checkDriverAssigningCorrectness(Map<String, Serializable> patchData) {
        var statusField = "status";
        if (!patchData.containsKey("driverId")
                && patchData.containsKey(statusField)
                && (String.valueOf(patchData.get(statusField)).equals(TripStatus.DRIVER_ASSIGNED.name()))) {
            throw new ConflictException(CAN_NOT_CHANGE_STATUS_STRING + patchData.get(statusField) + ", водитель не назначен");
        }
    }

    @Override
    public void checkPlanningIsAvailable(TripStatus status) {
        if (!TripStatus.WAITING_FOR_ASSIGNMENT.equals(status)) {
            throw new ConflictException("Нельзя запланировать заявку, которая находится в статусе: " + status);
        }
    }

    @Override
    public void checkDriverSwitchIsAvailable(Trip trip) {
        var possibleStatuses = List.of(TripStatus.SENT_TO_CONTRACTOR, TripStatus.WAITING_FOR_ASSIGNMENT,
                TripStatus.DRIVER_ASSIGNED);
        if (!possibleStatuses.contains(trip.getStatus())) {
            throw new ConflictException("Нельзя назначить другого водителя, когда поездка уже начата.");
        }
    }

    @Override
    public void checkManualOnlineSwitchAvailable(Shift shift) {
        if(shift.getEwbId() != null){
            throw new ConflictException("Вывод на линию/уход с линии доступен только через ЭПЛ");
        }
    }
}
