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
import ru.sber.transport.trip.business.dto.CheckinResponseDTO;
import ru.sber.transport.trip.business.dto.TripV2Dto;
import ru.sber.transport.trip.business.model.ActionType;
import ru.sber.transport.trip.business.model.Actor;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.*;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

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
class DriverControllerTest extends KafkaTest {

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

    private ContractorsRecord contractor;

    private DispatcherRecord dispatcher;

    private DriverRecord driver;

    private ShiftRecord shift;

    private TripsRecord trip1;

    private ObjectMapper objectMapper;

    @BeforeEach
    void createData() {
        AuthorizeUtils.authorize(manager);
        when(function.apply(any())).thenReturn(true);

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
        dispatcher.setOauthId(UUID.randomUUID());

        dslContext.insertInto(Tables.DISPATCHER).set(dispatcher).execute();

        trip1 = new TripsRecord();
        trip1.setId(UUID.randomUUID());
        trip1.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip1.setFactEndTime(trip1.getFactStartTime().plusHours(1));
        trip1.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip1.setExpectedEndTime(trip1.getExpectedStartTime().plusHours(1));
        trip1.setExpectedDistance(2d);
        trip1.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip1.setContractorId(contractor.getId());
        trip1.setWaypoints(JSON.json("[]"));
        trip1.setRequests(JSON.json("[]"));
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
        driver.setLatitude(55.644466);
        driver.setLongitude(37.395744);

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

        driver.setShiftId(shift.getId());
        driver.setOnline(true);
        dslContext.update(Tables.DRIVER).set(driver).execute();
    }

    @Test
    void testGetDriversByCoordinates() throws Exception {
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getOauthId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("longitude", "37.395744")
                        .param("latitude", "55.644466")
                        .param("fullSearch", "true")
                        .param("enableShiftFilter", "false")
                        .param("page", "0")
                        .param("size", "20")
                        .param("name", "FirstName")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {
        });
        Assertions.assertEquals(1, content.size());
        Assertions.assertEquals(driver.getId().toString(), content.get(0).get("id"));
        Assertions.assertNotNull(content.get(0).get("currentShift"));

        driver.setLongitude(null);
        driver.setLatitude(null);
        dslContext.update(Tables.DRIVER).set(driver).execute();

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("longitude", "37.395744")
                        .param("latitude", "55.644466")
                        .param("fullSearch", "true")
                        .param("enableShiftFilter", "false")
                        .param("page", "0")
                        .param("size", "20")
                        .param("name", "FirstName")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {
        });
        Assertions.assertEquals(1, content.size());
        Assertions.assertEquals(driver.getId().toString(), content.get(0).get("id"));
        Assertions.assertNotNull(content.get(0).get("currentShift"));

        driver.setLatitude(55.644466);
        driver.setLongitude(37.395744);
        driver.setPointTime(OffsetDateTime.now(ZoneOffset.UTC));
        dslContext.update(Tables.DRIVER).set(driver).execute();

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("longitude", "37.395744")
                        .param("latitude", "55.644466")
                        .param("fullSearch", "false")
                        .param("enableShiftFilter", "true")
                        .param("page", "0")
                        .param("size", "20")
                        .param("name", "FirstName")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {
        });
        Assertions.assertEquals(1, content.size());
        Assertions.assertEquals(driver.getId().toString(), content.get(0).get("id"));
        Assertions.assertNotNull(content.get(0).get("currentShift"));

        trip1.setDriverId(driver.getId());
        trip1.setStatus(TripStatus.DRIVER_ASSIGNED.name());
        trip1.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(2));
        dslContext.update(Tables.TRIPS_).set(trip1).execute();

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("longitude", "37.395744")
                        .param("latitude", "55.644466")
                        .param("fullSearch", "false")
                        .param("enableShiftFilter", "true")
                        .param("page", "0")
                        .param("size", "20")
                        .param("name", "FirstName")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {
        });
        Assertions.assertEquals(0, content.size());

        trip1.setDriverId(null);
        trip1.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        dslContext.update(Tables.TRIPS_).set(trip1).execute();

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("longitude", "37.395744")
                        .param("latitude", "55.644466")
                        .param("fullSearch", "false")
                        .param("enableShiftFilter", "true")
                        .param("tripStartDate", LocalDateTime.now(ZoneOffset.UTC).plusDays(2).toString())
                        .param("page", "0")
                        .param("size", "20")
                        .param("name", "FirstName")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {
        });
        Assertions.assertEquals(0, content.size());

        shift.setEndDate(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(2));
        dslContext.update(Tables.SHIFT).set(shift).execute();

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("longitude", "37.395744")
                        .param("latitude", "55.644466")
                        .param("fullSearch", "false")
                        .param("enableShiftFilter", "true")
                        .param("page", "0")
                        .param("size", "20")
                        .param("name", "FirstName")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {
        });
        Assertions.assertEquals(0, content.size());

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("longitude", "37.395744")
                        .param("latitude", "55.644466")
                        .param("fullSearch", "false")
                        .param("enableShiftFilter", "true")
                        .param("page", "0")
                        .param("size", "20")
                        .param("name", "FirstName")
                        .param("autoparkId", UUID.randomUUID().toString())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {
        });
        Assertions.assertEquals(0, content.size());

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("longitude", "37.395744")
                        .param("latitude", "55.644466")
                        .param("fullSearch", "true")
                        .param("enableShiftFilter", "true")
                        .param("page", "0")
                        .param("size", "20")
                        .param("name", "FirstName")
                        .param("autoparkId", UUID.randomUUID().toString())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {
        });
        Assertions.assertEquals(0, content.size());
    }

    @Test
    void getDriversByTripStartDateForPlanning() throws Exception {
        driver.setShiftId(null);
        driver.setOnline(false);
        dslContext.update(Tables.DRIVER).set(driver).execute();

        trip1.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC).plusDays(2));
        trip1.setExpectedStartTime(trip1.getExpectedStartTime().plusDays(1));
        trip1.setPlannedShiftId(shift.getId());

        dslContext.update(Tables.TRIPS_).set(trip1).execute();

        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("forPlanning", "true")
                        .param("tripStartDate", shift.getStartDate().toString())
                        .param("tripEndDate", shift.getEndDate().toString())
                        .param("tripStartDate", shift.getStartDate().toString())
                        .param("tripEndDate", shift.getEndDate().toString())
                        .param("page", "0")
                        .param("size", "20")
                        .param("name", "FirstName")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {
        });
        Assertions.assertEquals(1, content.size());
        Assertions.assertEquals(driver.getId().toString(), content.get(0).get("id"));
        Assertions.assertNotNull(content.get(0).get("currentShift"));

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("forPlanning", "true")
                        .param("tripStartDate", shift.getStartDate().toString())
                        .param("tripEndDate", shift.getEndDate().toString())
                        .param("tripStartDate", shift.getStartDate().toString())
                        .param("tripEndDate", shift.getEndDate().toString())
                        .param("page", "0")
                        .param("size", "20")
                        .param("name", "FirstName")
                        .param("autoparkId", UUID.randomUUID().toString())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {
        });
        Assertions.assertEquals(0, content.size());
    }

    @Test
    void testGetCurrentTrip() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        [
                            {
                                "field": "driverId",
                                "value": "%s"
                            }
                        ]
                        """.formatted(driver.getId()))
        ).andExpect(MockMvcResultMatchers.status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        [
                            {
                                "field": "status",
                                "value": "DRIVER_ON_THE_WAY"
                            },
                            {
                                "field": "changedByDriver",
                                "value": "true"
                            },
                            {
                                "field": "latitude",
                                "value": "55.83938831333333"
                            },
                            {
                                "field": "longitude",
                                "value": "49.12604832333333"
                            },
                            {
                                "field": "dateTime",
                                "value": "2023-10-23T12:35:25.515152600Z"
                            },
                            {
                                "field": "timezone",
                                "value": "GMT+03"
                            },
                            {
                                "field": "type",
                                "value": "MANUAL"
                            }
                        ]
                        """)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/driver/" + driver.getId() + "/trip/current/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var trip = objectMapper.readValue(response.getResponse().getContentAsString(), TripV2Dto.class);
        Assertions.assertNotNull(trip);
        Assertions.assertEquals(trip1.getId(), trip.id());
    }

    @Test
    void testUpdateFinalInfo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        [
                            {
                                "field": "driverId",
                                "value": "%s"
                            }
                        ]
                        """.formatted(driver.getId()))
        ).andExpect(MockMvcResultMatchers.status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        [
                            {
                                "field": "status",
                                "value": "DRIVER_ON_THE_WAY"
                            },
                            {
                                "field": "changedByDriver",
                                "value": "true"
                            },
                            {
                                "field": "latitude",
                                "value": "55.83938831333333"
                            },
                            {
                                "field": "longitude",
                                "value": "49.12604832333333"
                            },
                            {
                                "field": "dateTime",
                                "value": "2023-10-23T12:35:25.515152600Z"
                            },
                            {
                                "field": "timezone",
                                "value": "GMT+03"
                            },
                            {
                                "field": "type",
                                "value": "MANUAL"
                            }
                        ]
                        """)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.put("/contractor/" + contractor.getId().toString() + "/driver/" + driver.getId() + "/trip/" + trip1.getId() + "/final/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                             "tripFactInfo" : [
                                 {
                                     "status":"DRIVER_ARRIVED",
                                     "startTime":"1698048000000",
                                     "endTime":"1698048020000",
                                     "passedDistance":"2"
                                 },
                                 {
                                     "status":"INTERMEDIATE_WAYPOINT_ARRIVED",
                                     "startTime":"1698048020000",
                                     "endTime":"1698048030000",
                                     "passedDistance":"2"
                                 },
                                 {
                                     "status":"ORDER_FINISHED",
                                     "startTime":"1698048040000",
                                     "endTime":"1698048040000",
                                     "passedDistance":"2"
                                 }
                             ]
                         }
                                """)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        var trip = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        Assertions.assertNotNull(trip);
        Assertions.assertEquals(6, trip.getFactDistance());
        Assertions.assertEquals(30000, trip.getDriverWaitingTime());
    }

    @Test
    void testSetLastPointInfo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/self/last-point/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                              "latitude":"55.83938833333333",
                              "longitude":"49.12604833333333",
                              "timeZone":"GMT+03",
                              "currentTripId":"%s"
                          }
                                """.formatted(trip1.getId()))
        ).andExpect(MockMvcResultMatchers.status().isConflict());
        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        [
                            {
                                "field": "driverId",
                                "value": "%s"
                            }
                        ]
                        """.formatted(driver.getId()))
        ).andExpect(MockMvcResultMatchers.status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        [
                            {
                                "field": "status",
                                "value": "DRIVER_ON_THE_WAY"
                            },
                            {
                                "field": "changedByDriver",
                                "value": "true"
                            },
                            {
                                "field": "latitude",
                                "value": "55.83938831333333"
                            },
                            {
                                "field": "longitude",
                                "value": "49.12604832333333"
                            },
                            {
                                "field": "dateTime",
                                "value": "2023-10-23T12:35:25.515152600Z"
                            },
                            {
                                "field": "timezone",
                                "value": "GMT+03"
                            },
                            {
                                "field": "type",
                                "value": "MANUAL"
                            }
                        ]
                        """)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.put("/self/last-point/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                              "latitude":"55.83938833333333",
                              "longitude":"49.12604833333333",
                              "timeZone":"GMT+03",
                              "currentTripId":"%s"
                          }
                                """.formatted(trip1.getId()))
        ).andExpect(MockMvcResultMatchers.status().isOk());
        var actual = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(55.83938833333333, actual.getLatitude());
        Assertions.assertEquals(49.12604833333333, actual.getLongitude());
        Assertions.assertEquals("GMT+03", actual.getTimeZone());
    }

    @Test
    void testSetOnline() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/self/online/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                           "state":"false"
                        }
                            """)
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

        trip1 = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        trip1.setPlannedShiftId(shift.getId());
        dslContext.update(Tables.TRIPS_).set(trip1).execute();

        mockMvc.perform(MockMvcRequestBuilders.put("/self/online/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                           "state":"true"
                        }
                            """)
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

        var actualTrip = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
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
    void testGetTripCheckins() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        [
                            {
                                "field": "driverId",
                                "value": "%s"
                            }
                        ]
                        """.formatted(driver.getId()))
        ).andExpect(MockMvcResultMatchers.status().isOk());
        var statuses = List.of(TripStatus.DRIVER_ON_THE_WAY, TripStatus.DRIVER_ARRIVED, TripStatus.TRIP_IN_PROGRESS, TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED, TripStatus.TRIP_IN_PROGRESS, TripStatus.ORDER_FINISHED);
        for (TripStatus status : statuses) {
            mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/")
                    .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            [
                                {
                                    "field": "status",
                                    "value": "%s"
                                },
                                {
                                    "field": "changedByDriver",
                                    "value": "true"
                                },
                                {
                                    "field": "latitude",
                                    "value": "55.83938831333333"
                                },
                                {
                                    "field": "longitude",
                                    "value": "49.12604832333333"
                                },
                                {
                                    "field": "dateTime",
                                    "value": "2023-10-23T12:35:25.515152600Z"
                                },
                                {
                                    "field": "timezone",
                                    "value": "GMT+03"
                                },
                                {
                                    "field": "type",
                                    "value": "MANUAL"
                                }
                            ]
                            """.formatted(status.name()))
            ).andExpect(MockMvcResultMatchers.status().isOk());
        }

        var response = mockMvc.perform(MockMvcRequestBuilders.get("/self/trip/" + trip1.getId().toString() + "/checkin-info/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        var content = objectMapper.readValue(response.getResponse().getContentAsString(), CheckinResponseDTO.class);
        Assertions.assertNotNull(content);
        Assertions.assertEquals(6, content.getChekins().size());
        Assertions.assertNotNull(content.getTripDuration());
        Assertions.assertTrue(content.isComplete());
    }

}
