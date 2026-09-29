package ru.sber.transport.trip.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import ru.sber.transport.trip.business.dto.TripV2Dto;
import ru.sber.transport.trip.business.dto.v2.CheckinResponseDtoV2;
import ru.sber.transport.trip.business.model.ActionType;
import ru.sber.transport.trip.business.model.Actor;
import ru.sber.transport.trip.business.model.Request;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.*;
import ru.sber.transport.trip.business.dto.TripAssignStatisticDto;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

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
class TripControllerTest extends KafkaTest {

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

    private TripsRecord trip1;

    private TripsRecord trip2;

    private TripsRecord trip3;

    private ShiftRecord shift;

    private VehicleRecord vehicle;

    private ObjectMapper objectMapper;

    @BeforeEach
    void createData(){
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

        dslContext.insertInto(Tables.DISPATCHER).set(dispatcher).execute();

        trip1 = new TripsRecord();
        trip1.setId(UUID.randomUUID());
        trip1.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        trip1.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        trip1.setFactEndTime(trip1.getFactStartTime().plusHours(1));
        trip1.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        trip1.setExpectedEndTime(trip1.getExpectedStartTime().plusHours(1));
        trip1.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip1.setContractorId(contractor.getId());
        trip1.setWaypoints(JSON.json("[{\"id\":null,\"latitude\":55.731603,\"longitude\":37.625821,\"orderingIndex\":0,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT0S\",\"fullAddress\":\"Никулинская 12к1\",\"contact\":null,\"passengers\":[{\"type\":\"BOARDING\",\"firstName\":\"Иван\",\"patronymic\":\"Иванович\",\"phone\":\"+79991234567\"},{\"type\":\"BOARDING\",\"firstName\":\"Петр\",\"patronymic\":\"Петрович\",\"phone\":\"+79991234569\"}]},{\"id\":null,\"latitude\":55.731105,\"longitude\":37.625123,\"orderingIndex\":1,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT6M40S\",\"fullAddress\":\"Ленина 1\",\"contact\":null,\"passengers\":[{\"type\":\"UNBOARDING\",\"firstName\":\"Иван\",\"patronymic\":\"Иванович\",\"phone\":\"+79991234567\"},{\"type\":\"BOARDING\",\"firstName\":\"Сидор\",\"patronymic\":\"Сидорович\",\"phone\":\"+79991231234\"}]},{\"id\":null,\"latitude\":55.731113,\"longitude\":37.665321,\"orderingIndex\":2,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":\"PT0S\",\"fullAddress\":\"Красная площадь 1\",\"contact\":null,\"passengers\":[{\"type\":\"UNBOARDING\",\"firstName\":\"Петр\",\"patronymic\":\"Петрович\",\"phone\":\"+79991234569\"},{\"type\":\"UNBOARDING\",\"firstName\":\"Сидор\",\"patronymic\":\"Сидорович\",\"phone\":\"+79991231234\"}]}]"));
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
        trip2.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusDays(2));
        trip2.setFactEndTime(trip2.getFactStartTime().plusHours(1));
        trip2.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusDays(2));
        trip2.setExpectedEndTime(trip2.getExpectedStartTime().plusHours(1));
        trip2.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip2.setContractorId(contractor.getId());
        trip2.setWaypoints(JSON.json("[]"));
        trip2.setRequests(JSON.json("[]"));
        trip2.setDigitId(BigInteger.TWO);
        trip2.setFactDistance(10.02);
        trip2.setPassengerCount(1);
        trip2.setTaxiClass(TaxiClass.ECONOMY.name());
        trip2.setDriverWaitingTime(23000L);
        trip2.setDispatcherId(dispatcher.getId());
        trip2.setReportCreated(false);

        trip3 = new TripsRecord();
        trip3.setId(UUID.randomUUID());
        trip3.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip3.setFactEndTime(trip3.getFactStartTime().plusHours(1));
        trip3.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip3.setExpectedEndTime(trip3.getExpectedStartTime().plusHours(1));
        trip3.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip3.setContractorId(contractor.getId());
        trip3.setWaypoints(JSON.json("[]"));
        trip3.setRequests(JSON.json("[]"));
        trip3.setDigitId(BigInteger.valueOf(3L));
        trip3.setFactDistance(10.02);
        trip3.setPassengerCount(1);
        trip3.setTaxiClass(TaxiClass.ECONOMY.name());
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

    }


    @Test
    void getAllTest() throws Exception {
        trip1 = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        trip1.setPlannedShiftId(shift.getId());
        dslContext.update(Tables.TRIPS_).set(trip1).where(Tables.TRIPS_.ID.eq(trip1.getId())).execute();
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {});
        Assertions.assertEquals(3, content.size());
        var assertionList = List.of(trip1.getId(), trip2.getId(), trip3.getId());
        var forCheck = content.stream().filter(trip -> assertionList.contains(trip.id())).collect(Collectors.toList());
        Assertions.assertEquals(3, forCheck.size());
        forCheck = forCheck.stream().filter(trip -> trip.id().equals(trip1.getId())).collect(Collectors.toList());
        Assertions.assertEquals(1, forCheck.size());
        Assertions.assertNotNull(forCheck.get(0).planned());
        Assertions.assertNotNull(forCheck.get(0).planned().driver());
        Assertions.assertNotNull(forCheck.get(0).planned().vehicle());
        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/")
                        .param("humanReadableId", "TP-0001-00000001")
                        .param("desireDateStart", OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).minusMinutes(10).toString())
                        .param("desireDateEnd", OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).plusMinutes(10).toString())
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {});
        Assertions.assertEquals(1, content.size());
        Assertions.assertEquals(trip1.getId(), content.get(0).id());
        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/")
                        .param("requestHumanReadableId", "OT-0001-00009505")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {});
        Assertions.assertEquals(1, content.size());
        Assertions.assertEquals(trip1.getId(), content.get(0).id());
        Assertions.assertNotNull(content.get(0).creationTime());
    }

    @DisplayName("Проверка получения поездок по ID водителя")
    @Test
    void test_getAllByDriverIds() throws Exception {
        trip1 = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        trip1.setDriverId(driver.getId());
        dslContext.update(Tables.TRIPS_).set(trip1).where(Tables.TRIPS_.ID.eq(trip1.getId())).execute();
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/")
                        .param("driverIds", driver.getId().toString())
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {});
        Assertions.assertEquals(1, content.size());
        Assertions.assertEquals(trip1.getId(), content.get(0).id());
        Assertions.assertEquals(trip1.getDriverId(), content.get(0).driver().id());
        Assertions.assertNotNull(content.get(0).creationTime());
    }

    @DisplayName("Проверка получения поездок по ожидаемой длительности поездки")
    @Test
    void test_getAllByExpectedTime() throws Exception {
        trip1 = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        trip1.setExpectedTime(25200L);
        dslContext.update(Tables.TRIPS_).set(trip1).where(Tables.TRIPS_.ID.eq(trip1.getId())).execute();
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/")
                        .param("expectedTime", String.valueOf(6L))
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {});
        Assertions.assertEquals(1, content.size());
        Assertions.assertEquals(trip1.getId(), content.get(0).id());
        Assertions.assertEquals(trip1.getExpectedTime(), content.get(0).expectedTime());
    }

    @DisplayName("Проверка получения поездок по классу такси")
    @Test
    void test_getAllByTaxiClass() throws Exception {
        // Создаем новую поездку с классом BUSINESS
        var tripBusiness = new TripsRecord();
        tripBusiness.setId(UUID.randomUUID());
        tripBusiness.setDigitId(BigInteger.valueOf(100L));
        tripBusiness.setTaxiClass(TaxiClass.BUSINESS.name());
        tripBusiness.setContractorId(contractor.getId());
        tripBusiness.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        tripBusiness.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        tripBusiness.setFactEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(100));
        tripBusiness.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        tripBusiness.setExpectedEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(100));
        tripBusiness.setRequests(JSON.json(objectMapper.writeValueAsString(Set.of(createRequest(UUID.randomUUID())))));
        tripBusiness.setWaypoints(JSON.json(objectMapper.writeValueAsString(List.of())));
        tripBusiness.setReportCreated(false);
        dslContext.insertInto(Tables.TRIPS_).set(tripBusiness).execute();

        // Проверяем поиск по BUSINESS - должна вернуться только новая поездка
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/")
                        .param("taxiClass", TaxiClass.BUSINESS.name())
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {});
        Assertions.assertEquals(1, content.size());
        Assertions.assertEquals(tripBusiness.getId(), content.get(0).id());
        Assertions.assertEquals(TaxiClass.BUSINESS.name(), content.get(0).taxiClass());
        
        // Проверяем поиск по ECONOMY - должна вернуться 0 поездок (так как trip1 теперь BUSINESS, а trip2, trip3 ECONOMY)
        // Но мы создали новую поездку с BUSINESS, поэтому trip2 и trip3 остаются ECONOMY
        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/")
                        .param("taxiClass", TaxiClass.ECONOMY.name())
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {});
        // trip2 и trip3 имеют ECONOMY, поэтому их должно быть 2
        Assertions.assertEquals(2, content.size());
        var economyTripIds = content.stream().map(TripV2Dto::id).toList();
        Assertions.assertTrue(economyTripIds.contains(trip2.getId()));
        Assertions.assertTrue(economyTripIds.contains(trip3.getId()));
        Assertions.assertEquals(TaxiClass.ECONOMY.name(), content.get(0).taxiClass());
        Assertions.assertEquals(TaxiClass.ECONOMY.name(), content.get(1).taxiClass());
    }

    @DisplayName("Проверка получения статистики назначения трипов")
    @Test
    void test_getStatistic() throws Exception {
        createTrip(101, TripStatus.SENT_TO_CONTRACTOR, dispatcher.getContractorId());
        createTrip(102, TripStatus.WAITING_FOR_ASSIGNMENT, dispatcher.getContractorId());
        createTrip(103, TripStatus.DRIVER_ASSIGNED, dispatcher.getContractorId());
        createTrip(105, TripStatus.DRIVER_ON_THE_WAY, dispatcher.getContractorId());
        createTrip(106, TripStatus.DRIVER_ARRIVED, dispatcher.getContractorId());
        createTrip(107, TripStatus.TRIP_IN_PROGRESS, dispatcher.getContractorId());
        createTrip(108, TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED, dispatcher.getContractorId());
        createTrip(109, TripStatus.ORDER_CANCELLED_BY_CLIENT, dispatcher.getContractorId());

        var url = "/contractor/" + dispatcher.getContractorId() + "/statistic/";

        var response = mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var dto = objectMapper.readValue(response.getResponse().getContentAsString(), TripAssignStatisticDto.class);

        assertEquals(10, dto.getTotalCount());
        assertEquals(5, dto.getAssignCount());
        assertEquals(5, dto.getNotAssignCount());
    }

    @Test
    void getAllSortedTest() throws Exception {
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("field", "EXPECTED_START_TIME")
                        .param("direction", "DESC")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {});
        Assertions.assertEquals(3, content.size());
        Assertions.assertEquals(trip3.getId(), content.get(0).id());
        Assertions.assertEquals(trip1.getId(), content.get(1).id());
        Assertions.assertEquals(trip2.getId(), content.get(2).id());
    }

    @Test
    void testPatchTripByDispatcher() throws Exception {
        trip1.setDispatcherId(null);
        dslContext.update(Tables.TRIPS_).set(trip1).where(Tables.TRIPS_.ID.eq(trip1.getId())).execute();
        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                [
                                    {
                                        "field": "dispatcherTakeToWork",
                                        "value": "true"
                                    }
                                ]
                                """)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        var actualHistory = dslContext.selectFrom(Tables.TRIP_HISTORY)
                .where(Tables.TRIP_HISTORY.TRIP_ID.eq(trip1.getId()))
                .and(Tables.TRIP_HISTORY.ACTION.eq(ActionType.DISPATCHER_TAKE_TO_WORK.name()))
                .fetchOne();
        Assertions.assertNotNull(actualHistory);

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                [
                                    {
                                        "field": "dispatcherTakeToWork",
                                        "value": "true"
                                    },
                                    {
                                        "field": "driverId",
                                        "value": "%s"
                                    },
                                    {
                                        "field": "factDistance",
                                        "value": "12"
                                    },
                                    {
                                        "field": "driverWaitingTime",
                                        "value": "60000"
                                    },
                                    {
                                        "field": "expectedVehicleId",
                                        "value": "%s"
                                    },
                                    {
                                        "field": "factCost",
                                        "value": "9999"
                                    }
                                ]
                                """.formatted(driver.getId(), vehicle.getId()))
        ).andExpect(MockMvcResultMatchers.status().isOk());
        var actual = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(12.0, actual.getFactDistance());
        Assertions.assertEquals(60000, actual.getDriverWaitingTime());
        Assertions.assertEquals(driver.getId(), actual.getDriverId());
        Assertions.assertEquals(TripStatus.DRIVER_ASSIGNED.name(), actual.getStatus());
        Assertions.assertEquals(vehicle.getId(), actual.getExpectedVehicleId());
        Assertions.assertEquals(9999, actual.getFactCost());

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                [
                                    {
                                        "field": "status",
                                        "value": "DRIVER_ON_THE_WAY"
                                    }
                                ]
                                """)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        actual = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        var actualDriver = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(TripStatus.DRIVER_ON_THE_WAY.name(), actual.getStatus());
        Assertions.assertEquals(driver.getId(), actual.getDriverId());
        Assertions.assertNotNull(actualDriver);
        Assertions.assertEquals(actual.getId(), actualDriver.getActiveTripId());
        Assertions.assertEquals(false, actualDriver.getServing());

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                [
                                    {
                                        "field": "status",
                                        "value": "DRIVER_ARRIVED"
                                    }
                                ]
                                """)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        actual = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        actualDriver = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(TripStatus.DRIVER_ARRIVED.name(), actual.getStatus());
        Assertions.assertEquals(driver.getId(), actual.getDriverId());
        Assertions.assertNotNull(actualDriver);
        Assertions.assertEquals(actual.getId(), actualDriver.getActiveTripId());
        Assertions.assertEquals(false, actualDriver.getServing());

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                [
                                    {
                                        "field": "status",
                                        "value": "TRIP_IN_PROGRESS"
                                    }
                                ]
                                """)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        actual = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        actualDriver = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(TripStatus.TRIP_IN_PROGRESS.name(), actual.getStatus());
        Assertions.assertEquals(driver.getId(), actual.getDriverId());
        Assertions.assertNotNull(actualDriver);
        Assertions.assertEquals(actual.getId(), actualDriver.getActiveTripId());
        Assertions.assertEquals(true, actualDriver.getServing());

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                [
                                    {
                                        "field": "status",
                                        "value": "ORDER_FINISHED"
                                    }
                                ]
                                """)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        actual = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        actualDriver = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(TripStatus.ORDER_FINISHED.name(), actual.getStatus());
        Assertions.assertEquals(driver.getId(), actual.getDriverId());
        Assertions.assertNotNull(actualDriver);
        Assertions.assertNull(actualDriver.getActiveTripId());
        Assertions.assertEquals(false, actualDriver.getServing());

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip2.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                [
                                    {
                                        "field": "driverId",
                                        "value": "%s"
                                    },
                                    {
                                        "field": "factDistance",
                                        "value": "12"
                                    },
                                    {
                                        "field": "driverWaitingTime",
                                        "value": "60000"
                                    }
                                ]
                                """.formatted(driver.getId()))
        ).andExpect(MockMvcResultMatchers.status().isOk());
        actual = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(trip2.getId())).fetchOne();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(12.0, actual.getFactDistance());
        Assertions.assertEquals(60000, actual.getDriverWaitingTime());
        Assertions.assertEquals(driver.getId(), actual.getDriverId());
        Assertions.assertEquals(TripStatus.DRIVER_ASSIGNED.name(), actual.getStatus());

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip2.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                [
                                    {
                                        "field": "status",
                                        "value": "DRIVER_ON_THE_WAY"
                                    }
                                ]
                                """)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        actual = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(trip2.getId())).fetchOne();
        actualDriver = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(TripStatus.DRIVER_ON_THE_WAY.name(), actual.getStatus());
        Assertions.assertEquals(driver.getId(), actual.getDriverId());
        Assertions.assertNotNull(actualDriver);
        Assertions.assertEquals(actual.getId(), actualDriver.getActiveTripId());
        Assertions.assertEquals(false, actualDriver.getServing());

        shift.setEndDate(LocalDateTime.now(ZoneOffset.UTC));
        dslContext.update(Tables.SHIFT).set(shift).execute();

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip2.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                [
                                    {
                                        "field": "status",
                                        "value": "ORDER_CANCELLED_BY_DRIVER"
                                    }
                                ]
                                """)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        actual = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(trip2.getId())).fetchOne();
        actualDriver = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(TripStatus.ORDER_CANCELLED_BY_DRIVER.name(), actual.getStatus());
        Assertions.assertNotNull(actualDriver);
        Assertions.assertEquals(false, actualDriver.getOnline());
        Assertions.assertNull(actualDriver.getShiftId());

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip2.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                [
                                    {
                                        "field": "factCost",
                                        "value": "9999"
                                    }
                                ]
                                """.formatted(driver.getId()))
        ).andExpect(MockMvcResultMatchers.status().isOk());
        actual = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(trip2.getId())).fetchOne();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(9999, actual.getFactCost());
        Assertions.assertFalse(actual.getReportCreated());
    }

    @Test
    void testPatchTripByDispatcherForPlanning() throws Exception {
        driver.setShiftId(null);
        driver.setOnline(false);
        dslContext.update(Tables.DRIVER).set(driver).execute();

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                [
                                    {
                                        "field": "driverId",
                                        "value": "%s"
                                    },
                                    {
                                        "field": "planningShiftId",
                                        "value": "%s"
                                    }
                                ]
                                """.formatted(driver.getId(), shift.getId()))
        ).andExpect(MockMvcResultMatchers.status().isOk());
        var actual = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();

        var actualHistory = dslContext.selectFrom(Tables.TRIP_HISTORY)
                .where(Tables.TRIP_HISTORY.TRIP_ID.eq(trip1.getId())).fetchOne();

        Assertions.assertNotNull(actual);
        Assertions.assertNull(actual.getDriverId());
        Assertions.assertEquals(shift.getId(), actual.getPlannedShiftId());

        Assertions.assertNotNull(actualHistory);
        Assertions.assertEquals(ActionType.TRIP_PLANNING.name(), actualHistory.getAction());
        Assertions.assertEquals(shift.getId(), actualHistory.getNewPlannedShiftId());
        Assertions.assertNull(actualHistory.getOldDriverId());
        Assertions.assertNull(actualHistory.getNewDriverId());
        Assertions.assertEquals(actualHistory.getNewStatus(), actualHistory.getOldStatus());
        Assertions.assertNotNull(actualHistory.getActorId());
        Assertions.assertEquals(Actor.DISPATCHER.name(), actualHistory.getActorType());
    }

    @Test
    void testPatchTripByDispatcherForPlanningConflict() throws Exception {
        driver.setShiftId(null);
        driver.setOnline(false);
        dslContext.update(Tables.DRIVER).set(driver).execute();

        trip1 = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        trip1.setStatus(TripStatus.TRIP_IN_PROGRESS.name());
        dslContext.update(Tables.TRIPS_).set(trip1).execute();

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                [
                                    {
                                        "field": "driverId",
                                        "value": "%s"
                                    },
                                    {
                                        "field": "planningShiftId",
                                        "value": "%s"
                                    }
                                ]
                                """.formatted(driver.getId(), shift.getId()))
        ).andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    void testPatchTripByDriver() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                [
                                    {
                                        "field": "driverId",
                                        "value": "%s"
                                    },
                                    {
                                        "field": "factDistance",
                                        "value": "12"
                                    },
                                    {
                                        "field": "driverWaitingTime",
                                        "value": "60000"
                                    }
                                ]
                                """.formatted(driver.getId()))
        ).andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DRIVER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
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

        var actual = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();

        var actualCheckIns = Arrays.stream(dslContext.selectFrom(Tables.CHECK_IN)
                .where(Tables.CHECK_IN.TRIP_ID.eq(trip1.getId())).fetchArray()).toList();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(driver.getId(), actual.getDriverId());
        Assertions.assertEquals(TripStatus.DRIVER_ON_THE_WAY.name(), actual.getStatus());
        Assertions.assertEquals(1, actualCheckIns.size());
        Assertions.assertEquals(55.83938831333333, actualCheckIns.get(0).getLatitude());
        Assertions.assertEquals(49.12604832333333, actualCheckIns.get(0).getLongitude());
        Assertions.assertEquals("MANUAL", actualCheckIns.get(0).getType());

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DRIVER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            [
                                {
                                    "field": "status",
                                    "value": "DRIVER_ARRIVED"
                                },
                                {
                                    "field": "changedByDriver",
                                    "value": "true"
                                },
                                {
                                    "field": "latitude",
                                    "value": "55.83938831333133"
                                },
                                {
                                    "field": "longitude",
                                    "value": "49.12604232333333"
                                },
                                {
                                    "field": "dateTime",
                                    "value": "2023-10-23T12:55:25.515152600Z"
                                },
                                {
                                    "field": "timezone",
                                    "value": "GMT+03"
                                },
                                {
                                    "field": "type",
                                    "value": "AUTO"
                                }
                            ]
                                """)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DRIVER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            [
                                {
                                    "field": "status",
                                    "value": "TRIP_IN_PROGRESS"
                                },
                                {
                                    "field": "changedByDriver",
                                    "value": "true"
                                },
                                {
                                    "field": "latitude",
                                    "value": "55.83938831333133"
                                },
                                {
                                    "field": "longitude",
                                    "value": "49.12604232333333"
                                },
                                {
                                    "field": "dateTime",
                                    "value": "2023-10-23T12:55:25.515152600Z"
                                },
                                {
                                    "field": "timezone",
                                    "value": "GMT+03"
                                },
                                {
                                    "field": "type",
                                    "value": "AUTO"
                                }
                            ]
                                """)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DRIVER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            [
                                {
                                    "field": "status",
                                    "value": "ORDER_FINISHED"
                                },
                                {
                                    "field": "changedByDriver",
                                    "value": "true"
                                },
                                {
                                    "field": "latitude",
                                    "value": "55.83938831333133"
                                },
                                {
                                    "field": "longitude",
                                    "value": "49.12604232333333"
                                },
                                {
                                    "field": "dateTime",
                                    "value": "2023-10-23T12:55:25.515152600Z"
                                },
                                {
                                    "field": "timezone",
                                    "value": "GMT+03"
                                },
                                {
                                    "field": "type",
                                    "value": "AUTO"
                                }
                            ]
                                """)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        actual = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();

        actualCheckIns = Arrays.stream(dslContext.selectFrom(Tables.CHECK_IN)
                .where(Tables.CHECK_IN.TRIP_ID.eq(trip1.getId())).fetchArray())
                .sorted(Comparator.comparing(CheckInRecord::getTime)).toList();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(driver.getId(), actual.getDriverId());
        Assertions.assertEquals(TripStatus.ORDER_FINISHED.name(), actual.getStatus());
        Assertions.assertEquals(4, actualCheckIns.size());
        Assertions.assertEquals(55.83938831333133, actualCheckIns.get(1).getLatitude());
        Assertions.assertEquals(49.12604232333333, actualCheckIns.get(1).getLongitude());
        Assertions.assertEquals("AUTO", actualCheckIns.get(1).getType());

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DRIVER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            [
                                {
                                    "field": "status",
                                    "value": "DRIVER_ARRIVED"
                                },
                                {
                                    "field": "changedByDriver",
                                    "value": "true"
                                }
                            ]
                                """)
        ).andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    void testGetAllByDispatcher() throws Exception {
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/dispatcher/"+dispatcher.getId().toString()+"/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {});
        Assertions.assertEquals(2, content.size());
        var assertionList = List.of(trip1.getId(), trip2.getId());
        var forCheck = content.stream().filter(trip -> assertionList.contains(trip.id())).collect(Collectors.toList());
        Assertions.assertEquals(2, forCheck.size());
        Assertions.assertEquals(dispatcher.getId(), forCheck.get(0).dispatcher().id());
        Assertions.assertEquals(dispatcher.getId(), forCheck.get(1).dispatcher().id());
    }

    @Test
    void testGetTrip() throws Exception {
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId().toString()+"/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var trip = objectMapper.readValue(response.getResponse().getContentAsString(), TripV2Dto.class);
        Assertions.assertNotNull(trip);
        Assertions.assertEquals(trip1.getId(), trip.id());
    }

    @Test
    void testGetTripsByDriver() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
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

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip2.getId()+"/")
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

        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/driver/"+driver.getId().toString()+"/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DRIVER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {});
        assertEquals(2, content.size());
        var assertionList = List.of(trip1.getId(), trip2.getId());
        var forCheck = content.stream().filter(trip -> assertionList.contains(trip.id())).toList();
        assertEquals(2, forCheck.size());
        assertEquals(driver.getId(), forCheck.get(0).driver().id());
        assertEquals(driver.getId(), forCheck.get(1).driver().id());
    }

    @Test
    void testGetTripByDriver() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
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

        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/driver/"+driver.getId().toString()+"/trip/"+trip1.getId().toString()+"/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DRIVER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var trip = objectMapper.readValue(response.getResponse().getContentAsString(), TripV2Dto.class);
        Assertions.assertNotNull(trip);
        Assertions.assertEquals(trip1.getId(), trip.id());
    }

    @Test
    void testGetTripCheckins() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId()+"/")
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

        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId().toString()+"/checkin-info/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DRIVER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        var content = objectMapper.readValue(response.getResponse().getContentAsString(), CheckinResponseDtoV2.class);
        Assertions.assertNotNull(content);
        Assertions.assertTrue(content.getChekins().isEmpty());
        Assertions.assertNull(content.getTripDuration());
        Assertions.assertFalse(content.isComplete());

        var statuses = List.of(TripStatus.DRIVER_ON_THE_WAY, TripStatus.DRIVER_ARRIVED, TripStatus.TRIP_IN_PROGRESS, TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED, TripStatus.TRIP_IN_PROGRESS, TripStatus.ORDER_FINISHED);
        for (TripStatus status : statuses){
            mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/")
                    .with(jwt().jwt(builder -> builder.claim("scope", "DRIVER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
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

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/trip/"+trip1.getId().toString()+"/checkin-info/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DRIVER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        content = objectMapper.readValue(response.getResponse().getContentAsString(), CheckinResponseDtoV2.class);
        Assertions.assertNotNull(content);
        Assertions.assertEquals(6, content.getChekins().size());
        Assertions.assertNotNull(content.getTripDuration());
        Assertions.assertTrue(content.isComplete());
    }

    private void createTrip(int index, TripStatus status, UUID contractorId) throws JsonProcessingException {
        var trip = new TripsRecord();
        trip.setId(UUID.randomUUID());
        trip.setDigitId(BigInteger.valueOf(index));
        trip.setTaxiClass(TaxiClass.ECONOMY.name());
        trip.setContractorId(contractorId);
        trip.setStatus(status.name());
        trip.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setFactEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(index));
        trip.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setExpectedEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(index));
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

//    private void testIgnoreConflict(){
//        var tripProvider = mock(TripProvider.class);
//        var driverProvider = mock(DriverProvider.class);
//        var driverSender = mock(DriverSender.class);
//        var contractorProvider = mock(ContractorProvider.class);
//        var dispatcherProvider = mock(DispatcherProvider.class);
//        var shiftProvider = mock(ShiftProvider.class);
//        var shiftSender = mock(ShiftSender.class);
//        var tripHistoryProvider = mock(TripHistoryProvider.class);
//        var checkinProvider = mock(CheckinProvider.class);
//        var updateTripService = mock(UpdateTripService.class);
//        var verificationService = mock(VerificationService.class);
//        var dispatcherStatusChangingService = mock(DispatcherStatusChangingService.class);
//        var tripSenders = List.of(mock(TripSender.class));
//        var checkinMapper = mock(CheckinMapper.class);
//        var tripService = new TripServiceImpl(tripProvider, driverProvider, driverSender, contractorProvider,
//                dispatcherProvider, shiftProvider, shiftSender, tripHistoryProvider, checkinProvider, updateTripService,
//                verificationService, dispatcherStatusChangingService, tripSenders, checkinMapper);
//         tripService.
//    }

}
