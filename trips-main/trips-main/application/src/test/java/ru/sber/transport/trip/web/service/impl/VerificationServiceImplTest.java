package ru.sber.transport.trip.web.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.trip.business.dto.ShiftOperationType;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.web.exceptions.ConflictException;
import ru.sber.transport.trip.web.service.VerificationService;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Проверка сервиса проверок")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
class VerificationServiceImplTest {

    @Test
    @DisplayName("Проверка занятости водителя для смены статуса поездки")
    void checkDriverBusynessForStatusChanging() {
        var trip = new Trip();
        trip.setId(UUID.randomUUID());

        var driver = new Driver();
        driver.setActiveTripId(UUID.randomUUID());

        testWrongScenario(service -> service.checkDriverBusynessForStatusChanging(trip, driver));
    }

    @Test
    @DisplayName("Проверка конечного статуса")
    void checkEndStatus() {
        var trip = new Trip();
        trip.setStatus(TripStatus.ORDER_FINISHED);

        var driver = new Driver();
        driver.setActiveTripId(trip.getId());

        testWrongScenario(service -> service.checkEndStatus(trip, TripStatus.TRIP_IN_PROGRESS));
    }

    @Test
    @DisplayName("Проверка принадлежности водителя к поездке")
    void checkDriverToTripRelation() {
        var tripDriver = new Driver();
        tripDriver.setId(UUID.randomUUID());

        var currentDriver = new Driver();
        currentDriver.setId(UUID.randomUUID());

        testWrongScenario(service -> service.checkDriverToTripRelation(null, currentDriver));
    }

    @Test
    @DisplayName("Проверка принадлежности водителя к контрагенту поездки")
    void checkDriverToContractorRelation() {
        var trip = new Trip();
        trip.setContractorId(UUID.randomUUID());

        var driver = new Driver();
        driver.setContractorId(UUID.randomUUID());

        testWrongScenario(service -> service.checkDriverToContractorRelation(driver, trip));
    }

    @Test
    @DisplayName("Проверка принадлежности диспетчера к контрагенту поездки")
    void checkDispatcherToContractorRelation() {
        var trip = new Trip();
        trip.setContractorId(UUID.randomUUID());

        var dispatcher = new Dispatcher();
        dispatcher.setContractorId(UUID.randomUUID());

        testWrongScenario(service -> service.checkDispatcherToContractorRelation(dispatcher, trip));
    }

    @Test
    @DisplayName("Проверка отношения даты окончания смены к времени начала поездки")
    void checkShiftEndDateToTripStartTimeRelation() {
        var trip = new Trip();
        trip.setExpectedStartTime(OffsetDateTime.now().plusDays(1));

        var shift = new Shift();
        shift.setEndDate(LocalDateTime.now());

        testWrongScenario(service -> service.checkShiftEndDateToTripStartTimeRelation(shift, trip));
    }

    @Test
    @DisplayName("Проверка удаления смены")
    void checkShiftIsDeleted() {
        var shift = new Shift();
        shift.setDeleted(true);

        var operations = ShiftOperationType.values();
        Arrays.stream(operations).forEach(operation -> testWrongScenario(service -> service.checkShiftIsDeleted(shift, operation)));
    }

    @Test
    @DisplayName("Проверка принадлежности водителя и диспетчера к одному контрагенту")
    void checkDriverToDispatcherRelation() {
        var driver = new Driver();
        driver.setContractorId(UUID.randomUUID());

        var dispatcher = new Dispatcher();
        dispatcher.setContractorId(UUID.randomUUID());

        testWrongScenario(service -> service.checkDriverToDispatcherRelation(driver, dispatcher));
    }

    @Test
    @DisplayName("Проверка занятости водителя для выхода со смены")
    void checkDriverBusynessForExitFromShift() {
        var driver = new Driver();
        driver.setServing(true);

        testWrongScenario(service -> service.checkDriverBusynessForExitFromShift(driver));
    }

    @Test
    @DisplayName("Проверка существования смены")
    void checkShiftsExistence() {
        var shifts = new LinkedList<Shift>();

        testWrongScenario(service -> service.checkShiftsExistence(shifts));
    }

    @Test
    @DisplayName("Проверка существования чекинов")
    void checkCheckinsExistence() {
        testWrongScenario(service -> service.checkCheckinsExistence(0, 0));
    }

    @Test
    @DisplayName("Проверка отмененности поездки")
    void checkIsTripCancelled() {
        var hrid = UUID.randomUUID().toString();
        var trip = new Trip();
        trip.setStatus(TripStatus.ORDER_CANCELLED_BY_DRIVER);

        testWrongScenario(service -> service.checkIsTripCancelled(trip, hrid));
    }

    @Test
    @DisplayName("Проверка выхода на смену водителем")
    void checkIsDriverOnline() {
        var driver = new Driver();
        driver.setOnline(false);

        testWrongScenario(service -> service.checkIsDriverOnline(driver));
    }

    @Test
    @DisplayName("Проверка статуса на невозможность его примменения в конкретной ситуации")
    void checkImpossibleStatus() {
        var impossibleStatuses = List.of(TripStatus.SENT_TO_CONTRACTOR);

        testWrongScenario(service -> service.checkImpossibleStatus(impossibleStatuses, TripStatus.SENT_TO_CONTRACTOR));
    }

    @Test
    @DisplayName("Проверка статуса на возможность его применения в конкретной ситуации.")
    void checkPossibleStatus() {
        var possibleStatuses = List.of(TripStatus.SENT_TO_CONTRACTOR);

        testWrongScenario(service -> service.checkPossibleStatus(possibleStatuses, TripStatus.TRIP_IN_PROGRESS));
    }

    @Test
    @DisplayName("Проверка корректности назначения водителя")
    void checkDriverAssigningCorrectness() {
        var patchData = Map.of("status", (Serializable) TripStatus.DRIVER_ASSIGNED.name());

        testWrongScenario(service -> service.checkDriverAssigningCorrectness(patchData));
    }

    @Test
    @DisplayName("Проверка доступности планирования по поездке")
    void checkPlanningIsAvailable() {
        testWrongScenario(service -> service.checkPlanningIsAvailable(TripStatus.TRIP_IN_PROGRESS));
    }

    @Test
    @DisplayName("Проверка доступности переназначения водителя.")
    void checkDriverSwitchIsAvailable() {
        var trip = new Trip();
        trip.setStatus(TripStatus.ORDER_FINISHED);

        testWrongScenario(service -> service.checkDriverSwitchIsAvailable(trip));
    }

    void testWrongScenario(Consumer<VerificationService> func) {
        var service = new VerificationServiceImpl();

        try {
            func.accept(service);
            fail();
        } catch (ConflictException e) {
            // it has to throw
        }
    }
}
