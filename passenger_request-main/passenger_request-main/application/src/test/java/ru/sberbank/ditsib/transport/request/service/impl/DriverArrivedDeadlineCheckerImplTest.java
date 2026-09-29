package ru.sberbank.ditsib.transport.request.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.ContractorRepository;
import ru.sberbank.ditsib.transport.request.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForTaxiRepository;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.service.DriverArrivedDeadlineChecker;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка логики определения нарушения контрольных сроков")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@Transactional
@MockitoBean(types = JwtDecoder.class)
class DriverArrivedDeadlineCheckerImplTest extends KafkaTest {

    @Autowired
    private DriverArrivedDeadlineChecker driverArrivedDeadlineChecker;

    @Autowired
    private RequestForTaxiRepository repository;

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ContractorRepository contractorRepository;

    @Test
    @DisplayName("Водитель не приехал, текущее время больше КС")
    void execute_driver_not_arrived_deadline_violation_test() {
        var driverArrivedDeadline = LocalDateTime.now().minusHours(1);
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        var employee = Employee.builder()
                .id(UUID.randomUUID())
                .firstName("firstName")
                .lastName("lastName")
                .build();
        employee = employeeRepository.save(employee);

        var request1 = RequestForTaxi.builder()
                .deadlineState(null)
                .driverArrivedDatetime(null)
                .driverArrivedDeadline(driverArrivedDeadline)
                .status(TripRequestStatus.TAXI_APPROVED)
                .humanReadableId("humanReadableId1")
                .author(employee)
                .passenger(employee)
                .desiredDate(driverArrivedDeadline.minusHours(1))
                .coopTrip(false)
                .passengerCount(1)
                .sentToContractor(true)
                .deadlineState(DeadlineState.NONE)
                .transportType(TransportTypeEnum.TAXI)
                .contractorId(contractor.getId())
                .active(true)
                .build();

        repository.save(request1);

        var requestWithoutViolation = RequestForTaxi.builder()
                .deadlineState(null)
                .driverArrivedDatetime(driverArrivedDeadline.minusHours(1))
                .driverArrivedDeadline(driverArrivedDeadline)
                .status(TripRequestStatus.TAXI_APPROVED)
                .humanReadableId("humanReadableId2")
                .author(employee)
                .passenger(employee)
                .desiredDate(driverArrivedDeadline.minusHours(1))
                .coopTrip(false)
                .passengerCount(1)
                .sentToContractor(true)
                .deadlineState(DeadlineState.NONE)
                .active(true)
                .transportType(TransportTypeEnum.TAXI)
                .contractorId(contractor.getId())
                .build();

        repository.save(requestWithoutViolation);

        var result = driverArrivedDeadlineChecker.execute();

        assertEquals(1, result);
    }

    @Test
    @DisplayName("Водитель не приехал, текущее время меньше КС")
    void execute_driver_not_arrived_deadline_without_violation_test() {
        var driverArrivedDeadline = LocalDateTime.now().plusHours(1);
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        var employee = Employee.builder()
                .id(UUID.randomUUID())
                .firstName("firstName")
                .lastName("lastName")
                .build();
        employee = employeeRepository.save(employee);

        var request3 = RequestForTaxi.builder()
                .deadlineState(null)
                .driverArrivedDatetime(null)
                .driverArrivedDeadline(driverArrivedDeadline)
                .status(TripRequestStatus.TAXI_APPROVED)
                .humanReadableId("humanReadableId3")
                .author(employee)
                .passenger(employee)
                .desiredDate(driverArrivedDeadline.minusHours(1))
                .coopTrip(false)
                .passengerCount(1)
                .sentToContractor(true)
                .deadlineState(DeadlineState.NONE)
                .transportType(TransportTypeEnum.TAXI)
                .contractorId(contractor.getId())
                .active(true)
                .build();

        repository.save(request3);

        var requestWithoutViolation = RequestForTaxi.builder()
                .deadlineState(null)
                .driverArrivedDatetime(driverArrivedDeadline.minusHours(1))
                .driverArrivedDeadline(driverArrivedDeadline)
                .status(TripRequestStatus.TAXI_APPROVED)
                .humanReadableId("humanReadableId4")
                .author(employee)
                .passenger(employee)
                .desiredDate(driverArrivedDeadline.minusHours(1))
                .coopTrip(false)
                .passengerCount(1)
                .sentToContractor(true)
                .deadlineState(DeadlineState.NONE)
                .transportType(TransportTypeEnum.TAXI)
                .contractorId(contractor.getId())
                .active(true)
                .build();

        repository.save(requestWithoutViolation);

        var result = driverArrivedDeadlineChecker.execute();

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Водитель приехал, время подачи ТС больше КС")
    void execute_driver_arrived_deadline_violation_test() {
        var driverArrivedDeadline = LocalDateTime.now().minusHours(1);
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        var employee = Employee.builder()
                .id(UUID.randomUUID())
                .firstName("firstName")
                .lastName("lastName")
                .build();
        employee = employeeRepository.save(employee);

        var request1 = RequestForTaxi.builder()
                .deadlineState(null)
                .driverArrivedDatetime(driverArrivedDeadline.plusHours(1))
                .driverArrivedDeadline(driverArrivedDeadline)
                .status(TripRequestStatus.TAXI_APPROVED)
                .humanReadableId("humanReadableId5")
                .author(employee)
                .passenger(employee)
                .desiredDate(driverArrivedDeadline.minusHours(1))
                .coopTrip(false)
                .passengerCount(1)
                .sentToContractor(true)
                .deadlineState(DeadlineState.NONE)
                .transportType(TransportTypeEnum.TAXI)
                .contractorId(contractor.getId())
                .active(true)
                .build();

        repository.save(request1);

        var requestWithoutViolation = RequestForTaxi.builder()
                .deadlineState(null)
                .driverArrivedDatetime(driverArrivedDeadline.minusHours(1))
                .driverArrivedDeadline(driverArrivedDeadline)
                .status(TripRequestStatus.TAXI_APPROVED)
                .humanReadableId("humanReadableId6")
                .author(employee)
                .passenger(employee)
                .desiredDate(driverArrivedDeadline.minusHours(1))
                .coopTrip(false)
                .passengerCount(1)
                .sentToContractor(true)
                .deadlineState(DeadlineState.NONE)
                .active(true)
                .transportType(TransportTypeEnum.TAXI)
                .contractorId(contractor.getId())
                .build();

        repository.save(requestWithoutViolation);

        var result = driverArrivedDeadlineChecker.execute();

        assertEquals(1, result);
    }
}