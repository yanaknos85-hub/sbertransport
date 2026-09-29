package ru.sber.transport.trip.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.ConsentFunction;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trip.business.dto.DriverBusynessDTO;
import ru.sber.transport.trip.business.dto.VehicleBusynessDTO;
import ru.sber.transport.trip.business.model.ActionType;
import ru.sber.transport.trip.business.model.Actor;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.*;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
class DispatcherControllerTest extends KafkaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DSLContext dslContext;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private AuthorizationManager<?> manager;

    @MockBean
    private ConsentFunction function;

    private DispatcherRecord dispatcher;

    private DriverRecord driver;

    private VehicleRecord vehicle;

    private ShiftRecord shift;

    private TripsRecord trip3;

    private TripsRecord trip4;

    private TripsRecord trip5;

    private CheckInRecord checkIn2;

    private CheckInRecord checkIn3;

    @BeforeEach
    void createData() {
        AuthorizeUtils.authorize(manager);
        when(function.apply(any())).thenReturn(true);

        var contractor = new ContractorsRecord();
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
        driver.setLatitude(55.644466);
        driver.setLongitude(37.395744);

        dslContext.insertInto(Tables.DRIVER).set(driver).execute();

        vehicle = new VehicleRecord();
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

        driver.setShiftId(shift.getId());
        driver.setOnline(true);
        dslContext.update(Tables.DRIVER).set(driver).execute();

        var trip1 = new TripsRecord();
        trip1.setId(UUID.randomUUID());
        trip1.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip1.setFactEndTime(trip1.getFactStartTime().plusHours(1));
        trip1.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip1.setExpectedEndTime(trip1.getExpectedStartTime().plusHours(1));
        trip1.setStatus(TripStatus.DRIVER_ASSIGNED.name());
        trip1.setContractorId(contractor.getId());
        trip1.setWaypoints(JSON.json("[{\"id\":null,\"latitude\":55.731603,\"longitude\":37.625821,\"orderingIndex\":0,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT0S\",\"fullAddress\":\"Никулинская 12к1\",\"contact\":null,\"passengers\":[{\"type\":\"BOARDING\",\"firstName\":\"Иван\",\"patronymic\":\"Иванович\",\"phone\":\"+79991234567\"},{\"type\":\"BOARDING\",\"firstName\":\"Петр\",\"patronymic\":\"Петрович\",\"phone\":\"+79991234569\"}]},{\"id\":null,\"latitude\":55.731105,\"longitude\":37.625123,\"orderingIndex\":1,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT6M40S\",\"fullAddress\":\"Ленина 1\",\"contact\":null,\"passengers\":[{\"type\":\"UNBOARDING\",\"firstName\":\"Иван\",\"patronymic\":\"Иванович\",\"phone\":\"+79991234567\"},{\"type\":\"BOARDING\",\"firstName\":\"Сидор\",\"patronymic\":\"Сидорович\",\"phone\":\"+79991231234\"}]},{\"id\":null,\"latitude\":55.731113,\"longitude\":37.665321,\"orderingIndex\":2,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT0S\",\"fullAddress\":\"Красная площадь 1\",\"contact\":null,\"passengers\":[{\"type\":\"UNBOARDING\",\"firstName\":\"Петр\",\"patronymic\":\"Петрович\",\"phone\":\"+79991234569\"},{\"type\":\"UNBOARDING\",\"firstName\":\"Сидор\",\"patronymic\":\"Сидорович\",\"phone\":\"+79991231234\"}]}]"));
        trip1.setRequests(JSON.json("[]"));
        trip1.setDigitId(BigInteger.ONE);
        trip1.setFactDistance(10.02);
        trip1.setPassengerCount(1);
        trip1.setTaxiClass(TaxiClass.COMFORT.name());
        trip1.setDriverWaitingTime(23000L);
        trip1.setDispatcherId(dispatcher.getId());
        trip1.setDriverId(driver.getId());
        trip1.setVehicleId(vehicle.getId());
        trip1.setReportCreated(false);

        dslContext.insertInto(Tables.TRIPS_).set(trip1).execute();

        var trip2 = new TripsRecord();
        trip2.setId(UUID.randomUUID());
        trip2.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip2.setFactEndTime(trip1.getFactStartTime().plusHours(1));
        trip2.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip2.setExpectedEndTime(trip1.getExpectedStartTime().plusHours(1));
        trip2.setStatus(TripStatus.ORDER_FINISHED.name());
        trip2.setContractorId(contractor.getId());
        trip2.setWaypoints(JSON.json("[{\"id\":null,\"latitude\":55.731603,\"longitude\":37.625821,\"orderingIndex\":0,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT0S\",\"fullAddress\":\"Никулинская 12к1\",\"contact\":null,\"passengers\":[{\"type\":\"BOARDING\",\"firstName\":\"Иван\",\"patronymic\":\"Иванович\",\"phone\":\"+79991234567\"},{\"type\":\"BOARDING\",\"firstName\":\"Петр\",\"patronymic\":\"Петрович\",\"phone\":\"+79991234569\"}]},{\"id\":null,\"latitude\":55.731105,\"longitude\":37.625123,\"orderingIndex\":1,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT6M40S\",\"fullAddress\":\"Ленина 1\",\"contact\":null,\"passengers\":[{\"type\":\"UNBOARDING\",\"firstName\":\"Иван\",\"patronymic\":\"Иванович\",\"phone\":\"+79991234567\"},{\"type\":\"BOARDING\",\"firstName\":\"Сидор\",\"patronymic\":\"Сидорович\",\"phone\":\"+79991231234\"}]},{\"id\":null,\"latitude\":55.731113,\"longitude\":37.665321,\"orderingIndex\":2,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT0S\",\"fullAddress\":\"Красная площадь 1\",\"contact\":null,\"passengers\":[{\"type\":\"UNBOARDING\",\"firstName\":\"Петр\",\"patronymic\":\"Петрович\",\"phone\":\"+79991234569\"},{\"type\":\"UNBOARDING\",\"firstName\":\"Сидор\",\"patronymic\":\"Сидорович\",\"phone\":\"+79991231234\"}]}]"));
        trip2.setRequests(JSON.json("[]"));
        trip2.setDigitId(BigInteger.TWO);
        trip2.setFactDistance(10.02);
        trip2.setPassengerCount(1);
        trip2.setTaxiClass(TaxiClass.COMFORT.name());
        trip2.setDriverWaitingTime(23000L);
        trip2.setDispatcherId(dispatcher.getId());
        trip2.setDriverId(driver.getId());
        trip2.setReportCreated(false);
        trip2.setVehicleId(vehicle.getId());

        dslContext.insertInto(Tables.TRIPS_).set(trip2).execute();

        var checkIn1 = new CheckInRecord();
        checkIn1.setId(UUID.randomUUID());
        checkIn1.setTripId(trip2.getId());
        checkIn1.setTimeZone("+3");
        checkIn1.setTime(OffsetDateTime.now());
        checkIn1.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.toString());
        dslContext.insertInto(Tables.CHECK_IN)
                .set(checkIn1)
                .execute();

        trip3 = new TripsRecord();
        trip3.setId(UUID.randomUUID());
        trip3.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip3.setFactEndTime(trip1.getFactStartTime().plusHours(1));
        trip3.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip3.setExpectedEndTime(trip1.getExpectedStartTime().plusHours(1));
        trip3.setStatus(TripStatus.ORDER_FINISHED.name());
        trip3.setContractorId(contractor.getId());
        trip3.setWaypoints(JSON.json("[{\"id\":null,\"latitude\":55.731603,\"longitude\":37.625821,\"orderingIndex\":0,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT0S\",\"fullAddress\":\"Никулинская 12к1\",\"contact\":null,\"passengers\":[{\"type\":\"BOARDING\",\"firstName\":\"Иван\",\"patronymic\":\"Иванович\",\"phone\":\"+79991234567\"},{\"type\":\"BOARDING\",\"firstName\":\"Петр\",\"patronymic\":\"Петрович\",\"phone\":\"+79991234569\"}]},{\"id\":null,\"latitude\":55.731105,\"longitude\":37.625123,\"orderingIndex\":1,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT6M40S\",\"fullAddress\":\"Ленина 1\",\"contact\":null,\"passengers\":[{\"type\":\"UNBOARDING\",\"firstName\":\"Иван\",\"patronymic\":\"Иванович\",\"phone\":\"+79991234567\"},{\"type\":\"BOARDING\",\"firstName\":\"Сидор\",\"patronymic\":\"Сидорович\",\"phone\":\"+79991231234\"}]},{\"id\":null,\"latitude\":55.731113,\"longitude\":37.665321,\"orderingIndex\":2,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT0S\",\"fullAddress\":\"Красная площадь 1\",\"contact\":null,\"passengers\":[{\"type\":\"UNBOARDING\",\"firstName\":\"Петр\",\"patronymic\":\"Петрович\",\"phone\":\"+79991234569\"},{\"type\":\"UNBOARDING\",\"firstName\":\"Сидор\",\"patronymic\":\"Сидорович\",\"phone\":\"+79991231234\"}]}]"));
        trip3.setRequests(JSON.json("[]"));
        trip3.setDigitId(BigInteger.valueOf(3));
        trip3.setFactDistance(10.02);
        trip3.setPassengerCount(1);
        trip3.setTaxiClass(TaxiClass.COMFORT.name());
        trip3.setDriverWaitingTime(23000L);
        trip3.setDispatcherId(dispatcher.getId());
        trip3.setDriverId(driver.getId());
        trip3.setReportCreated(false);
        trip3.setVehicleId(vehicle.getId());

        dslContext.insertInto(Tables.TRIPS_).set(trip3).execute();

        checkIn2 = new CheckInRecord();
        checkIn2.setId(UUID.randomUUID());
        checkIn2.setTripId(trip3.getId());
        checkIn2.setTimeZone("+3");
        checkIn2.setTime(OffsetDateTime.now());
        checkIn2.setStatus(TripStatus.DRIVER_ON_THE_WAY.toString());
        dslContext.insertInto(Tables.CHECK_IN)
                .set(checkIn2)
                .execute();

        trip4 = new TripsRecord();
        trip4.setId(UUID.randomUUID());
        trip4.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip4.setExpectedEndTime(trip4.getExpectedStartTime().plusHours(1));
        trip4.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip4.setFactEndTime(trip4.getFactStartTime().plusHours(1));
        trip4.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip4.setContractorId(contractor.getId());
        trip4.setWaypoints(JSON.json("[{\"id\":null,\"latitude\":55.731603,\"longitude\":37.625821,\"orderingIndex\":0,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT0S\",\"fullAddress\":\"Никулинская 12к1\",\"contact\":null,\"passengers\":[{\"type\":\"BOARDING\",\"firstName\":\"Иван\",\"patronymic\":\"Иванович\",\"phone\":\"+79991234567\"},{\"type\":\"BOARDING\",\"firstName\":\"Петр\",\"patronymic\":\"Петрович\",\"phone\":\"+79991234569\"}]},{\"id\":null,\"latitude\":55.731105,\"longitude\":37.625123,\"orderingIndex\":1,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT6M40S\",\"fullAddress\":\"Ленина 1\",\"contact\":null,\"passengers\":[{\"type\":\"UNBOARDING\",\"firstName\":\"Иван\",\"patronymic\":\"Иванович\",\"phone\":\"+79991234567\"},{\"type\":\"BOARDING\",\"firstName\":\"Сидор\",\"patronymic\":\"Сидорович\",\"phone\":\"+79991231234\"}]},{\"id\":null,\"latitude\":55.731113,\"longitude\":37.665321,\"orderingIndex\":2,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT0S\",\"fullAddress\":\"Красная площадь 1\",\"contact\":null,\"passengers\":[{\"type\":\"UNBOARDING\",\"firstName\":\"Петр\",\"patronymic\":\"Петрович\",\"phone\":\"+79991234569\"},{\"type\":\"UNBOARDING\",\"firstName\":\"Сидор\",\"patronymic\":\"Сидорович\",\"phone\":\"+79991231234\"}]}]"));
        trip4.setRequests(JSON.json("[]"));
        trip4.setDigitId(BigInteger.valueOf(4));
        trip4.setFactDistance(10.02);
        trip4.setPassengerCount(1);
        trip4.setTaxiClass(TaxiClass.COMFORT.name());
        trip4.setDriverWaitingTime(23000L);
        trip4.setDispatcherId(dispatcher.getId());
        trip4.setReportCreated(false);
        trip4.setExpectedVehicleId(vehicle.getId());

        dslContext.insertInto(Tables.TRIPS_).set(trip4).execute();

        trip5 = new TripsRecord();
        trip5.setId(UUID.randomUUID());
        trip5.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip5.setExpectedEndTime(trip5.getExpectedStartTime().plusHours(1));
        trip5.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip5.setFactEndTime(trip5.getFactStartTime().plusHours(1));
        trip5.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip5.setContractorId(contractor.getId());
        trip5.setWaypoints(JSON.json("[{\"id\":null,\"latitude\":55.731603,\"longitude\":37.625821,\"orderingIndex\":0,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT0S\",\"fullAddress\":\"Никулинская 12к1\",\"contact\":null,\"passengers\":[{\"type\":\"BOARDING\",\"firstName\":\"Иван\",\"patronymic\":\"Иванович\",\"phone\":\"+79991234567\"},{\"type\":\"BOARDING\",\"firstName\":\"Петр\",\"patronymic\":\"Петрович\",\"phone\":\"+79991234569\"}]},{\"id\":null,\"latitude\":55.731105,\"longitude\":37.625123,\"orderingIndex\":1,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT6M40S\",\"fullAddress\":\"Ленина 1\",\"contact\":null,\"passengers\":[{\"type\":\"UNBOARDING\",\"firstName\":\"Иван\",\"patronymic\":\"Иванович\",\"phone\":\"+79991234567\"},{\"type\":\"BOARDING\",\"firstName\":\"Сидор\",\"patronymic\":\"Сидорович\",\"phone\":\"+79991231234\"}]},{\"id\":null,\"latitude\":55.731113,\"longitude\":37.665321,\"orderingIndex\":2,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT0S\",\"fullAddress\":\"Красная площадь 1\",\"contact\":null,\"passengers\":[{\"type\":\"UNBOARDING\",\"firstName\":\"Петр\",\"patronymic\":\"Петрович\",\"phone\":\"+79991234569\"},{\"type\":\"UNBOARDING\",\"firstName\":\"Сидор\",\"patronymic\":\"Сидорович\",\"phone\":\"+79991231234\"}]}]"));
        trip5.setRequests(JSON.json("[]"));
        trip5.setDigitId(BigInteger.valueOf(5));
        trip5.setFactDistance(10.02);
        trip5.setPassengerCount(1);
        trip5.setTaxiClass(TaxiClass.COMFORT.name());
        trip5.setDriverWaitingTime(23000L);
        trip5.setDispatcherId(dispatcher.getId());
        trip5.setReportCreated(false);
        trip5.setPlannedShiftId(shift.getId());

        dslContext.insertInto(Tables.TRIPS_).set(trip5).execute();
    }

    @Test
    void testSetOnline() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/self/dispatcher/driver/" + driver.getId().toString() + "/online-switcher/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
        ).andExpect(MockMvcResultMatchers.status().isOk());

        var actualDriver = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();
        Assertions.assertNotNull(actualDriver);
        Assertions.assertEquals(false, actualDriver.getOnline());
        Assertions.assertNull(actualDriver.getShiftId());

        var actualShift = dslContext.selectFrom(Tables.SHIFT)
                .where(Tables.SHIFT.ID.eq(shift.getId())).fetchOne();
        Assertions.assertNotNull(actualShift);
        Assertions.assertEquals(false, actualShift.getActive());

        trip4 = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip4.getId())).fetchOne();
        trip4.setPlannedShiftId(shift.getId());
        dslContext.update(Tables.TRIPS_).set(trip4).execute();

        mockMvc.perform(MockMvcRequestBuilders.put("/self/dispatcher/driver/" + driver.getId().toString() + "/online-switcher/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
        ).andExpect(MockMvcResultMatchers.status().isOk());

        actualDriver = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();
        Assertions.assertNotNull(actualDriver);
        Assertions.assertEquals(true, actualDriver.getOnline());
        Assertions.assertEquals(shift.getId(), actualDriver.getShiftId());

        actualShift = dslContext.selectFrom(Tables.SHIFT)
                .where(Tables.SHIFT.ID.eq(shift.getId())).fetchOne();
        Assertions.assertNotNull(actualShift);
        Assertions.assertEquals(true, actualShift.getActive());

        var actualTrip = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip4.getId())).fetchOne();
        Assertions.assertEquals(shift.getDriverId(), actualTrip.getDriverId());
        Assertions.assertEquals(shift.getVehicleId(), actualTrip.getVehicleId());
        Assertions.assertNull(actualTrip.getPlannedShiftId());
        Assertions.assertEquals(TripStatus.DRIVER_ASSIGNED.name(), actualTrip.getStatus());

        var actualHistory = dslContext.selectFrom(Tables.TRIP_HISTORY)
                .where(Tables.TRIP_HISTORY.TRIP_ID.eq(actualTrip.getId())).fetchOne();
        Assertions.assertNotNull(actualHistory);
        Assertions.assertEquals(ActionType.DRIVER_CHANGING.name(), actualHistory.getAction());
        Assertions.assertEquals(shift.getId(), actualHistory.getOldPlannedShiftId());
        Assertions.assertNull(actualHistory.getNewPlannedShiftId());
        Assertions.assertNull(actualHistory.getOldDriverId());
        Assertions.assertEquals(shift.getDriverId(), actualHistory.getNewDriverId());
        Assertions.assertEquals(TripStatus.DRIVER_ASSIGNED.name(), actualHistory.getNewStatus());
        Assertions.assertEquals(TripStatus.WAITING_FOR_ASSIGNMENT.name(), actualHistory.getOldStatus());
        Assertions.assertEquals(actualHistory.getOldDispatcherId(), actualHistory.getNewDispatcherId());
        Assertions.assertNull(actualHistory.getActorId());
        Assertions.assertEquals(Actor.SYSTEM.name(), actualHistory.getActorType());
    }

    @Test
    void testGetDriverBusyness() throws Exception {
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/self/dispatcher/driver/busyness/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER")
                                        .claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "startTime":"%s",
                                    "endTime":"%s",
                                    "driverIds": [
                                        "%s"
                                    ],
                                    "includeOrderedVehicles": true
                                }
                                """.formatted(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).toString(),
                                OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).toString(),
                                driver.getId()))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        var result = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<DriverBusynessDTO>() {});
        assert result != null;
        Assertions.assertEquals(1, result.getBusyness().size());
        Assertions.assertEquals(driver.getId(), result.getBusyness().get(0).getDriver().getId());
        Assertions.assertEquals(driver.getHumanReadableId(), result.getBusyness().get(0).getDriver().getHumanReadableId());
        Assertions.assertEquals(4, result.getBusyness().get(0).getTrips().size());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getId());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getHumanReadableId());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getStatus());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getExpectedStartTime());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getExpectedEndTime());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getVehicle().getId());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getVehicle().getStateNumber());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getVehicle().getVehicleType());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getVehicle().getModel().getBrand());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getVehicle().getModel().getName());
        Assertions.assertEquals("Никулинская 12к1", result.getBusyness().get(0).getTrips().get(0).getStartAddress().getName());
        Assertions.assertEquals(55.731603, result.getBusyness().get(0).getTrips().get(0).getStartAddress().getLatitude());
        Assertions.assertEquals(37.625821, result.getBusyness().get(0).getTrips().get(0).getStartAddress().getLongitude());
        Assertions.assertTrue(result.getBusyness().get(0).getTrips().get(0).getPassenger().contains("Иван Иванович"));
        Assertions.assertTrue(result.getBusyness().get(0).getTrips().get(0).getPassenger().contains("Петр Петрович"));
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getId());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getHumanReadableId());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getStatus());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getFactStartTime());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getFactEndTime());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getVehicle().getId());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getVehicle().getStateNumber());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getVehicle().getVehicleType());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getVehicle().getModel().getBrand());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getVehicle().getModel().getName());
        Assertions.assertEquals(1, result.getOrders().size());
        Assertions.assertNotNull(result.getOrders().get(0).getId());
        Assertions.assertNotNull(result.getOrders().get(0).getHumanReadableId());
        Assertions.assertNotNull(result.getOrders().get(0).getStatus());
        Assertions.assertNotNull(result.getOrders().get(0).getExpectedStartTime());
        Assertions.assertNotNull(result.getOrders().get(0).getExpectedEndTime());
        Assertions.assertNotNull(result.getOrders().get(0).getVehicle().getId());
        Assertions.assertNotNull(result.getOrders().get(0).getVehicle().getStateNumber());
        Assertions.assertNotNull(result.getOrders().get(0).getVehicle().getVehicleType());
        Assertions.assertNotNull(result.getOrders().get(0).getVehicle().getModel().getBrand());
        Assertions.assertNotNull(result.getOrders().get(0).getVehicle().getModel().getName());

        assertThat(result.getOrders().get(0).getDriverProcessingTime())
                .isNull();

        assertThat(result.getBusyness().get(0).getTrips())
                .allSatisfy(trip -> {
                    if (trip.getId().equals(trip3.getId())) {
                        assertThat(trip.getDriverProcessingTime())
                                .isNotNull();

                        assertThat(trip.getDriverProcessingTime().truncatedTo(ChronoUnit.MILLIS))
                                .isEqualTo(checkIn2.getTime().truncatedTo(ChronoUnit.MILLIS));
                    } else {
                        assertThat(trip.getDriverProcessingTime())
                                .isNull();
                    }
                });
    }

    @Test
    void testGetVehicleBusyness() throws Exception {
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/self/dispatcher/vehicle/busyness/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER")
                                        .claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "startTime":"%s",
                                    "endTime":"%s",
                                    "vehicleIds": [
                                        "%s"
                                    ],
                                    "contractorId":"%s"
                                }
                                """.formatted(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).toString(),
                                OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).toString(),
                                vehicle.getId(), dispatcher.getContractorId()))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        var result = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<List<VehicleBusynessDTO>>() {});
        assert result != null;
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(vehicle.getId(), result.get(0).getVehicleId());
        Assertions.assertEquals(5, result.get(0).getTrips().size());
    }
}