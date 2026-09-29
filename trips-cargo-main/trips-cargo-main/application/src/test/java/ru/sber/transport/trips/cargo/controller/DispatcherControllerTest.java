package ru.sber.transport.trips.cargo.controller;

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
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trips.cargo.business.dto.DriverBusynessDTO;
import ru.sber.transport.trips.cargo.business.dto.VehicleBusynessDTO;
import ru.sber.transport.trips.cargo.business.model.ActionType;
import ru.sber.transport.trips.cargo.business.model.Actor;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips_cargo")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
class DispatcherControllerTest extends KafkaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DSLContext dslContext;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private AuthorizationManager<?> manager;

    private DispatcherRecord dispatcher;

    private DriverRecord driver;

    private ShiftRecord shift;

    private TripsRecord trip4;

    private VehicleRecord vehicle;

    @BeforeEach
    void createData(){
        AuthorizeUtils.authorize(manager);

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

        var trip1 = new TripsRecord();
        trip1.setId(UUID.randomUUID());
        trip1.setStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        trip1.setEndTime(trip1.getStartTime().plusHours(1));
        trip1.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip1.setContractorId(contractor.getId());
        trip1.setWaypoints(JSON.json("[]"));
        trip1.setRequests(JSON.json("[]"));
        trip1.setDigitId(BigInteger.ONE);
        trip1.setFactDistance(10.02);
        trip1.setCapacity(1.0);
        trip1.setLoadersWorkTime(23000L);
        trip1.setDispatcherId(dispatcher.getId());

        dslContext.insertInto(Tables.TRIPS).set(trip1).execute();

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

        var trip2 = new TripsRecord();
        trip2.setId(UUID.randomUUID());
        trip2.setStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip2.setEndTime(trip2.getStartTime().plusHours(1));
        trip2.setStatus(TripStatus.ORDER_FINISHED.name());
        trip2.setContractorId(contractor.getId());
        trip2.setWaypoints(JSON.json("[]"));
        trip2.setRequests(JSON.json("[]"));
        trip2.setDigitId(BigInteger.TWO);
        trip2.setFactDistance(10.02);
        trip2.setDispatcherId(dispatcher.getId());
        trip2.setDriverId(driver.getId());
        trip2.setVehicleId(vehicle.getId());

        dslContext.insertInto(Tables.TRIPS).set(trip2).execute();

        var trip3 = new TripsRecord();
        trip3.setId(UUID.randomUUID());
        trip3.setStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip3.setEndTime(trip3.getStartTime().plusHours(1));
        trip3.setStatus(TripStatus.ORDER_FINISHED.name());
        trip3.setContractorId(contractor.getId());
        trip3.setWaypoints(JSON.json("[]"));
        trip3.setRequests(JSON.json("[]"));
        trip3.setDigitId(BigInteger.valueOf(3));
        trip3.setFactDistance(10.02);
        trip3.setDispatcherId(dispatcher.getId());
        trip3.setDriverId(driver.getId());
        trip3.setVehicleId(vehicle.getId());

        dslContext.insertInto(Tables.TRIPS).set(trip3).execute();

        trip4 = new TripsRecord();
        trip4.setId(UUID.randomUUID());
        trip4.setStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip4.setEndTime(trip4.getStartTime().plusHours(1));
        trip4.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip4.setContractorId(contractor.getId());
        trip4.setWaypoints(JSON.json("[]"));
        trip4.setRequests(JSON.json("[]"));
        trip4.setDigitId(BigInteger.valueOf(4));
        trip4.setFactDistance(10.02);
        trip4.setDispatcherId(dispatcher.getId());

        dslContext.insertInto(Tables.TRIPS).set(trip4).execute();

    }

    @Test
    void testSetOnline() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/self/dispatcher/driver/"+driver.getId().toString()+"/online-switcher/")
                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
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

        trip4 = dslContext.selectFrom(Tables.TRIPS).where(Tables.TRIPS.ID.eq(trip4.getId())).fetchOne();
        trip4.setPlannedShiftId(shift.getId());
        dslContext.update(Tables.TRIPS).set(trip4).execute();

        mockMvc.perform(MockMvcRequestBuilders.put("/self/dispatcher/driver/"+driver.getId().toString()+"/online-switcher/")
                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
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

        var actualTrip = dslContext.selectFrom(Tables.TRIPS).where(Tables.TRIPS.ID.eq(trip4.getId())).fetchOne();
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
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString()).claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "startTime":"%s",
                            "endTime":"%s",
                            "driverIds": [
                                "%s"
                            ]
                        }
                        """.formatted(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).toString(),
                                OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).toString(),
                                driver.getId()))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<DriverBusynessDTO>() {});
        assert result != null;
        Assertions.assertEquals(1, result.getBusyness().size());
        Assertions.assertEquals(driver.getId(), result.getBusyness().get(0).getDriver().getId());
        Assertions.assertEquals(driver.getHumanReadableId(), result.getBusyness().get(0).getDriver().getHumanReadableId());
        Assertions.assertEquals(2, result.getBusyness().get(0).getTrips().size());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getId());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getHumanReadableId());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getStatus());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getStartTime());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getEndTime());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getVehicle().getId());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getVehicle().getStateNumber());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getVehicle().getVehicleType());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getVehicle().getModel().getBrand());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(0).getVehicle().getModel().getName());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getId());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getHumanReadableId());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getStatus());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getStartTime());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getEndTime());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getVehicle().getId());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getVehicle().getStateNumber());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getVehicle().getVehicleType());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getVehicle().getModel().getBrand());
        Assertions.assertNotNull(result.getBusyness().get(0).getTrips().get(1).getVehicle().getModel().getName());

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
        Assertions.assertEquals(2, result.get(0).getTrips().size());
    }
}
