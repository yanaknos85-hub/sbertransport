package ru.sber.transport.trip.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.trip.TripApplication;
import ru.sber.transport.trip.business.model.Request;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.*;
import ru.sber.transport.trip.messaging.senders.dispatcher.DriverSender;
import ru.sber.transport.trip.messaging.senders.dispatcher.ShiftSender;
import ru.sber.transport.trip.messaging.senders.dispatcher.TripSender;
import ru.sber.transport.trip.web.service.TripService;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка сервиса поездок")
@TestPropertySource(properties = "conflict.changing-state-of-finished-trip.ignore=true")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
public class TripServiceTest extends KafkaTest {

    @Autowired
    private TripService tripService;

    @Autowired
    private DSLContext dslContext;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private AuthorizationManager<?> manager;

    private ContractorsRecord contractor;

    private DispatcherRecord dispatcher;

    private DriverRecord driver;

    private TripsRecord trip1;

    private TripsRecord trip2;

    private TripsRecord trip3;

    private ShiftRecord shift;

    private ObjectMapper objectMapper;

    @BeforeEach
    void createData(){
        AuthorizeUtils.authorize(manager);

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

        trip1 = new TripsRecord();
        trip1.setId(UUID.randomUUID());
        trip1.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        trip1.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip1.setFactEndTime(trip1.getFactStartTime().plusHours(1));
        trip1.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip1.setExpectedEndTime(trip1.getExpectedStartTime().plusHours(1));
        trip1.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip1.setContractorId(contractor.getId());
        trip1.setWaypoints(JSON.json("[]"));
        trip1.setRequests(JSON.json("[{\"id\":\"63f0d17c-3b88-4500-becd-1a90f04494ab\",\"authorId\":\"30d6bac3-c1bc-4ce3-a6a4-e15f3d489914\",\"humanReadableId\":\"OT-0001-00009505\",\"author\":{\"id\":null,\"lastName\":\"Бландин\",\"firstName\":\"Александр\",\"patronymic\":\"Дмитриевич\",\"mobilePhone\":\"+78001307001\",\"email\":null,\"organization\":null},\"passengerId\":\"30d6bac3-c1bc-4ce3-a6a4-e15f3d489914\",\"passenger\":{\"id\":null,\"lastName\":\"Бландин\",\"firstName\":\"Александр\",\"patronymic\":\"Дмитриевич\",\"mobilePhone\":\"+78001307001\",\"email\":null,\"organization\":null},\"taxiClass\":\"ECONOMY\",\"passengerCount\":1,\"expected\":{\"cost\":1335.0,\"distance\":11.214,\"time\":1217.000000000},\"creationTime\":\"2023-10-23T10:45:12.000000307+00:00\",\"desiredDate\":\"2023-10-23T10:50:12.000000172+00:00\",\"rideId\":null,\"suburb\":false,\"timeZone\":\"GMT+03\",\"requestOptions\":null,\"tariffId\":\"11d54b73-11e4-4b37-af22-1a24173e1f04\",\"contractorId\":\"108ce2a2-c054-4dbf-9f23-34499dd69a59\",\"status\":\"TAXI_TRIP_FINISHED\",\"comment\":null,\"waypoints\":[{\"id\":\"42366678-6583-449e-816a-bf561cf4b2ba\",\"latitude\":55.75198989085822,\"longitude\":37.60040860924344,\"orderingIndex\":0,\"country\":\"Россия\",\"region\":\"Москва\",\"city\":\"Москва\",\"street\":\"Арбат\",\"house\":\"1\",\"building\":null,\"waitingTime\":\"PT0S\"},{\"id\":\"aad7aaa6-e853-4fbd-9189-d51fcbb6df07\",\"latitude\":55.74339357458361,\"longitude\":37.54573687155735,\"orderingIndex\":1,\"country\":\"Россия\",\"region\":\"Москва\",\"city\":\"Москва\",\"street\":\"Дунаевского\",\"house\":\"1\",\"building\":null,\"waitingTime\":\"PT0S\"}],\"driverWaitingTime\":\"PT1M\",\"factDistance\":2.0,\"transportType\":\"TAXI\",\"coop\":false}]"));
        trip1.setDigitId(BigInteger.ONE);
        trip1.setFactDistance(10.02);
        trip1.setPassengerCount(1);
        trip1.setTaxiClass(TaxiClass.COMFORT.name());
        trip1.setDriverWaitingTime(23000L);
        trip1.setDispatcherId(dispatcher.getId());
        trip1.setReportCreated(false);

        trip2 = new TripsRecord();
        trip2.setId(UUID.randomUUID());
        trip2.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip2.setFactEndTime(trip2.getFactStartTime().plusHours(1));
        trip2.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip2.setExpectedEndTime(trip2.getExpectedStartTime().plusHours(1));
        trip2.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip2.setContractorId(contractor.getId());
        trip2.setWaypoints(JSON.json("[]"));
        trip2.setRequests(JSON.json("[]"));
        trip2.setDigitId(BigInteger.TWO);
        trip2.setFactDistance(10.02);
        trip2.setPassengerCount(1);
        trip2.setTaxiClass(TaxiClass.COMFORT.name());
        trip2.setDriverWaitingTime(23000L);
        trip2.setDispatcherId(dispatcher.getId());
        trip2.setReportCreated(false);

        trip3 = new TripsRecord();
        trip3.setId(UUID.randomUUID());
        trip3.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip3.setExpectedEndTime(trip3.getExpectedStartTime().plusHours(1));
        trip3.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip3.setFactEndTime(trip3.getFactStartTime().plusHours(1));
        trip3.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip3.setContractorId(contractor.getId());
        trip3.setWaypoints(JSON.json("[]"));
        trip3.setRequests(JSON.json("[]"));
        trip3.setDigitId(BigInteger.valueOf(3L));
        trip3.setFactDistance(10.02);
        trip3.setPassengerCount(1);
        trip3.setTaxiClass(TaxiClass.COMFORT.name());
        trip3.setDriverWaitingTime(23000L);
        trip3.setReportCreated(false);

        dslContext.insertInto(Tables.TRIPS_).set(trip1).execute();
        dslContext.insertInto(Tables.TRIPS_).set(trip2).execute();
        dslContext.insertInto(Tables.TRIPS_).set(trip3).execute();

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

        shift = new ShiftRecord();
        shift.setId(UUID.randomUUID());
        shift.setContractorId(contractor.getId());
        shift.setDriverId(driver.getId());
        shift.setVehicleId(vehicle.getId());
        shift.setStartDate(LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
        shift.setEndDate(LocalDateTime.now(ZoneOffset.UTC).plusDays(1));
        shift.setActive(true);
        shift.setDeleted(false);

        dslContext.insertInto(Tables.SHIFT).set(shift).execute();

        trip1 = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        trip1.setStatus("DRIVER_ASSIGNED");
        trip1.setDriverId(driver.getId());
        dslContext.update(Tables.TRIPS_).set(trip1).execute();

        driver.setShiftId(shift.getId());
        driver.setOnline(true);
        dslContext.update(Tables.DRIVER).set(driver).execute();

    }

    @Disabled
    @Test
    void testIgnoreConflict() throws JsonProcessingException {
        var data = Map.of("status", (Serializable)"ORDER_FINISHED", "changedByDriver", "true");
        var jwt = Jwt
                .withTokenValue("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiJ9.eyJyb2xlcyI6WyJST0xFX0VNUExPWUVFX0NPUlBfQ0xJRU5UIiwiUk9MRV9FTkdJTkVFUl9DT1JQX0NMSUVOVCIsIlJPTEVfREVMRUdBVEVfUkVRVUVTVF9DT1JQX0NMSUVOVCIsIlJPTEVfQ0hJRUZfQ09SUF9DTElFTlQiLCJST0xFX0RJU1BBVENIRVJfQ09SUF9DTElFTlQiLCJST0xFX0FETUlOX0NPUlBfQ0xJRU5UIiwiUk9MRV9ESVNQQVRDSEVSX0NPTlRSQUNUT1IiLCJST0xFX0RSSVZFUl9DT05UUkFDVE9SIiwiUk9MRV9NQUlOVEVOQU5DRV9FTkdJTkVFUiIsIlJPTEVfQURNSU5fREFUQV9NQVNURVIiXSwicmFuZG9tIjoiNzZlNWFjNzMtMmQ0YS00Njk5LTk1YWMtZGRjNjllOThhNDE0IiwiZmFjdG9yIjoiQkFTSUMiLCJyZXF1ZXN0SWQiOiIyOGFjZjNjNTZhYjQxYzM1IiwiZXhwIjoxNzA5NzA0MTk1LCJpc3MiOiJTYmVyVHJhbnNwb3J0IiwiaWF0IjoxNzA5NzAzMjk1LCJzdWIiOiJCbGFuZGluQS1EIiwianRpIjoiMzBkNmJhYzMtYzFiYy00Y2UzLWE2YTQtZTE1ZjNkNDg5OTE0IiwidHJhbnNwb3J0IjpmYWxzZSwiZGF0YV9tYXN0ZXIiOnRydWV9.V7-JyQQDw_Ewog078hf4qPDUMypjYXO5cGsB4TtrEYWa0ZdJDZyJ3J9UwXPOypezAMEJ-8RhnNP1yMEwkUfUJ4jrhZpMtsCDB_pyM6V4XPas5hMNHYmC0vttiMcc2fNI5VOpMAvqjT1XolgQ5QoolgUEAlCydz_zAibVNbe0V_dT_E6Cr4oAXQINikOM3pdvhnrix8hM4MqTXgSJaVo-xyPcl0Zb0m1kZZ_A5v4xFoSuivD3vQN3GJbcOdgmFuSLVjL1qIbLyU8afmv31646CCtzcKElxBaC0cGrS2f7OsHPS8292cZffzBZk7Au1bkttmpGHg5czMcTBKrSfaA3orpD1vo7SDwYJQNwxEDPz_l8CL_gRN9-LOK-iey2EWnIW98es9RnAynHmTPHSUErDdpB7tEF8aGnO86uTXsIBpMmfhE_0vYusaM9XYHZBN1buF0bOiSoTzR0wKHHTvQ3EP-Wp0LRTBg7GXER_9R1gz2rQgoNEcMahC7x9rmmqL-nBeTGSFWPSqWjVwSNqJvUTT1KJYJJpwq1kKjvIw2mquWaQLleC8ulNABMdnBDsJVm-yUED_MTKiPrzThFu-Zt0vyxhL3rxcrKwUtmDmt-B3vub-GWZ70EDx8XmAiVVvTCr2Mu9-FTpp3rjbrwAJv-Q_0z_SoLEHdXK4zqEA6Id6Y")
                .header("typ", "JWT")
                .header("alg","RS512")
                .claim("iss","SberTransport")
                .claim("random","76e5ac73-2d4a-4699-95ac-ddc69e98a414")
                .claim("factor","BASIC")
                .claim("requestId","28acf3c56ab41c35")
                .claim("sub","BlandinA-D")
                .claim("jti",driver.getId())
                .claim("transport",false)
                .claim("data_master",true).build();

        tripService.update(contractor.getId(), trip1.getId(), data, new JwtAuthenticationToken(jwt));

        var count = dslContext.selectCount()
                .from(Tables.TRIP_HISTORY)
                .where(Tables.TRIP_HISTORY.TRIP_ID.eq(trip1.getId()))
                .and(Tables.TRIP_HISTORY.NEW_STATUS.eq("ORDER_FINISHED"))
                .fetchInto(Integer.class).get(0);

        Assertions.assertNotNull(count);
        assertEquals(1, count.intValue());

        tripService.update(contractor.getId(), trip1.getId(), data, new JwtAuthenticationToken(jwt));

        count = dslContext.selectCount()
                .from(Tables.TRIP_HISTORY)
                .where(Tables.TRIP_HISTORY.TRIP_ID.eq(trip1.getId()))
                .and(Tables.TRIP_HISTORY.NEW_STATUS.eq("ORDER_FINISHED"))
                .fetchInto(Integer.class).get(0);

        Assertions.assertNotNull(count);
        assertEquals(1, count.intValue());

    }

    @DisplayName("Проверка получения статистики по назначению трипов")
    @Test
    void test_getStatistic() throws JsonProcessingException {
        var contractorId = UUID.randomUUID();

        createTrip(1, TripStatus.SENT_TO_CONTRACTOR, contractorId);
        createTrip(2, TripStatus.WAITING_FOR_ASSIGNMENT, contractorId);
        createTrip(3, TripStatus.DRIVER_ASSIGNED, contractorId);
        createTrip(5, TripStatus.DRIVER_ON_THE_WAY, contractorId);
        createTrip(6, TripStatus.DRIVER_ARRIVED, contractorId);
        createTrip(7, TripStatus.TRIP_IN_PROGRESS, contractorId);
        createTrip(8, TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED, contractorId);
        createTrip(9, TripStatus.ORDER_CANCELLED_BY_CLIENT, contractorId);

        var dto = tripService.getAssignStatistic(contractorId, null);

        assertEquals(7, dto.getTotalCount());
        assertEquals(5, dto.getAssignCount());
        assertEquals(2, dto.getNotAssignCount());
    }

    private void createTrip(int index, TripStatus status, UUID contractorId) throws JsonProcessingException {
        var trip = new TripsRecord();
        trip.setId(UUID.randomUUID());
        trip.setDigitId(BigInteger.valueOf(index));
        trip.setTaxiClass(TaxiClass.ECONOMY.name());
        trip.setContractorId(contractorId);
        trip.setStatus(status.name());
        trip.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setExpectedEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(index));
        trip.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setFactEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(index));
        trip.setRequests(JSON.json(objectMapper.writeValueAsString(Set.of(createRequest(UUID.randomUUID())))));
        trip.setWaypoints(JSON.json(objectMapper.writeValueAsString(List.of())));
        trip.setReportCreated(false);

        dslContext.insertInto(Tables.TRIPS_).set(trip).execute();
    }

    private Request createRequest(UUID id) {
        var request = new Request();

        request.setTariffId(UUID.randomUUID());
        request.setId(id);

        return request;
    }
}
