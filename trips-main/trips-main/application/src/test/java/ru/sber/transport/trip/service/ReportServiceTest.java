package ru.sber.transport.trip.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.trip.business.model.Checkin;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.*;
import ru.sber.transport.trip.messaging.senders.ReportSender;
import ru.sber.transport.trip.providers.checkin.CheckinProvider;
import ru.sber.transport.trip.providers.trips.mapping.TripMapper;
import ru.sber.transport.trip.web.service.ReportService;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trip_reports.message.TripReportMessage;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка сервиса составления отчетов")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
public class ReportServiceTest extends KafkaTest {
    @Autowired
    private ReportService reportService;

    @Autowired
    private DSLContext dslContext;

    private ContractorsRecord contractor;

    private DispatcherRecord dispatcher;

    private DriverRecord driver;

    private TripsRecord trip1;

    private ObjectMapper objectMapper;

    @Mock
    private TripProvider tripProvider;
    @Mock
    private TripMapper tripMapper;
    @Mock
    private ReportSender reportSender;

    @BeforeEach
    void createData() throws JsonProcessingException {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        contractor = new ContractorsRecord();
        contractor.setId(UUID.randomUUID());
        contractor.setDigitId(BigInteger.ONE);
        contractor.setAutoassign(true);

        dslContext.insertInto(Tables.CONTRACTORS).set(contractor).execute();

        dispatcher = new DispatcherRecord();
        dispatcher.setId(UUID.randomUUID());
        dispatcher.setHumanReadableId("DS-0001-0001");
        dispatcher.setLastName("LastNameDispatcher");
        dispatcher.setFirstName("FirstNameDispatcher");
        dispatcher.setPatronymic("PatronymicDispatcher");
        dispatcher.setPhone("+70327749923");
        dispatcher.setEmail("disp@mail.ru");
        dispatcher.setContractorId(contractor.getId());

        dslContext.insertInto(Tables.DISPATCHER).set(dispatcher).execute();

        var stringRequests = "[{\"id\":\"63f0d17c-3b88-4500-becd-1a90f04494ab\",\"authorId\":\"30d6bac3-c1bc-4ce3-a6a4-e15f3d489914\",\"humanReadableId\":\"OT-0001-00009505\",\"author\":{\"id\":null,\"lastName\":\"Бландин\",\"firstName\":\"Александр\",\"patronymic\":\"Дмитриевич\",\"mobilePhone\":\"+78001307001\",\"email\":null,\"organization\":null},\"passengerId\":\"30d6bac3-c1bc-4ce3-a6a4-e15f3d489914\",\"passenger\":{\"id\":null,\"lastName\":\"Бландин\",\"firstName\":\"Александр\",\"patronymic\":\"Дмитриевич\",\"mobilePhone\":\"+78001307001\",\"email\":null,\"organization\":null},\"taxiClass\":\"ECONOMY\",\"passengerCount\":1,\"expected\":{\"cost\":1335.0,\"distance\":11.214,\"time\":1217.000000000},\"creationTime\":\"2023-10-23T10:45:12.000000307+00:00\",\"desiredDate\":\"2023-10-23T10:50:12.000000172+00:00\",\"rideId\":null,\"suburb\":false,\"timeZone\":\"GMT+03\",\"requestOptions\":null,\"tariffId\":\"11d54b73-11e4-4b37-af22-1a24173e1f04\",\"contractorId\":\"108ce2a2-c054-4dbf-9f23-34499dd69a59\",\"status\":\"TAXI_TRIP_FINISHED\",\"comment\":null,\"waypoints\":[{\"id\":\"42366678-6583-449e-816a-bf561cf4b2ba\",\"latitude\":55.75198989085822,\"longitude\":37.60040860924344,\"orderingIndex\":0,\"country\":\"Россия\",\"region\":\"Москва\",\"city\":\"Москва\",\"street\":\"Арбат\",\"house\":\"1\",\"building\":null,\"waitingTime\":\"PT0S\"},{\"id\":\"aad7aaa6-e853-4fbd-9189-d51fcbb6df07\",\"latitude\":55.74339357458361,\"longitude\":37.54573687155735,\"orderingIndex\":1,\"country\":\"Россия\",\"region\":\"Москва\",\"city\":\"Москва\",\"street\":\"Дунаевского\",\"house\":\"1\",\"building\":null,\"waitingTime\":\"PT0S\"}],\"driverWaitingTime\":\"PT1M\",\"factDistance\":2.0,\"transportType\":\"TAXI\",\"coop\":false}]";
        var stringWaypoints = "[{\"id\":\"a4b63256-146b-45ad-a9fc-496318a5dd89\",\"latitude\":55.6120850000000004,\"longitude\":37.2006659999999982,\"orderingIndex\":1,\"country\":\"Россия\",\"region\":\"Москва\",\"city\":\"Москва\",\"street\":\"улица Ленина\",\"house\":\"5\",\"building\":null,\"waitingTime\":20},{\"id\":\"11401c3b-e37e-4261-9f01-55da3316d7e5\",\"latitude\":55.779477,\"longitude\":37.6444290000000024,\"orderingIndex\":2,\"country\":\"Россия\",\"region\":\"Москва\",\"city\":\"Москва\",\"street\":\"Протопоповский переулок\",\"house\":\"40\",\"building\":null,\"waitingTime\":20}]";

        trip1 = new TripsRecord();
        trip1.setId(UUID.randomUUID());
        trip1.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(28));
        trip1.setFactEndTime(trip1.getFactStartTime().plusHours(1));
        trip1.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(28));
        trip1.setExpectedEndTime(trip1.getExpectedStartTime().plusHours(1));
        trip1.setStatus(TripStatus.ORDER_FINISHED.name());
        trip1.setContractorId(contractor.getId());
        trip1.setWaypoints(JSON.json(stringWaypoints));
        trip1.setRequests(JSON.json(stringRequests));
        trip1.setDigitId(BigInteger.ONE);
        trip1.setFactDistance(10.02);
        trip1.setPassengerCount(1);
        trip1.setTaxiClass(TaxiClass.COMFORT.name());
        trip1.setDriverWaitingTime(23000L);
        trip1.setDispatcherId(dispatcher.getId());
        trip1.setReportCreated(false);

        dslContext.insertInto(Tables.TRIPS_).set(trip1).execute();

        driver = new DriverRecord();
        driver.setId(UUID.randomUUID());
        driver.setHumanReadableId("DR-0001-0001");
        driver.setLastName("LastName");
        driver.setFirstName("FirstName");
        driver.setPatronymic("Patronymic");
        driver.setContractorId(contractor.getId());
        driver.setActive(true);
        driver.setRating(2);
        driver.setServing(false);
        driver.setOnline(false);
        driver.setContactPhone("+79399912274");
        driver.setEmail("mail@mail.ru");

        dslContext.insertInto(Tables.DRIVER).set(driver).execute();

        var vehicle = new VehicleRecord();
        vehicle.setId(UUID.randomUUID());
        vehicle.setBrand("Brand");
        vehicle.setModel("Model");
        vehicle.setColor("Color");
        vehicle.setContractorId(contractor.getId());
        vehicle.setDeleted(false);
        vehicle.setStateNumber("A992AA178RUS");

        dslContext.insertInto(Tables.VEHICLE).set(vehicle).execute();

        var shift = new ShiftRecord();
        shift.setId(UUID.randomUUID());
        shift.setContractorId(contractor.getId());
        shift.setDriverId(driver.getId());
        shift.setVehicleId(vehicle.getId());
        shift.setStartDate(LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
        shift.setEndDate(LocalDateTime.now(ZoneOffset.UTC).plusDays(1));
        shift.setActive(true);
        shift.setDeleted(false);

        dslContext.insertInto(Tables.SHIFT).set(shift).execute();

        driver.setShiftId(shift.getId());
        driver.setOnline(true);
        driver.setLatitude(55.6120850000000004);
        driver.setLongitude(37.2006659999999982);
        driver.setPointTime(OffsetDateTime.now());
        driver.setTimeZone("GMT+03");
        dslContext.update(Tables.DRIVER).set(driver).execute();

    }

    @Test
    void processReportTest(){
        reportService.processReport();
        var actualTrip = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(trip1.getId()))
                .fetchOne();
        assert actualTrip != null;
        Assertions.assertTrue(actualTrip.getReportCreated());
    }

    @Test
     void testProcessReport_ExceptionHandling() {
        // Arrange
        UUID tripId = UUID.randomUUID();
        TripsRecord record = new TripsRecord();
        record.setId(tripId);

        List<TripsRecord> trips = Collections.singletonList(record);
        when(tripProvider.findAllByReportCreatedFalseAndTerminalStatusesAndLimit(anyInt())).thenReturn(trips);

        when(tripMapper.toModel(any(TripsRecord.class))).thenThrow(new RuntimeException("Error"));

        // Act
        reportService.processReport();

        // Assert
        verify(reportSender, never()).send(any(TripReportMessage.class));
        verify(tripProvider, never()).setReportCreatedIsTrueByIds(anyList());
    }




}
