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
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest(properties = "authorization.consent-check=true")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка проверки подписания ПДн")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
public class CheckConsentTest extends KafkaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DSLContext dslContext;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private AuthorizationManager<?> manager;

    private ContractorsRecord contractor;

    private DispatcherRecord dispatcher;

    private DriverRecord driver;

    private ShiftRecord shift;

    private TripsRecord trip1;

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
        dispatcher.setConsent(true);

        dslContext.insertInto(Tables.DISPATCHER).set(dispatcher).execute();

        trip1 = new TripsRecord();
        trip1.setId(UUID.randomUUID());
        trip1.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip1.setFactEndTime(trip1.getFactStartTime().plusHours(1));
        trip1.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip1.setExpectedEndTime(trip1.getExpectedStartTime().plusHours(1));
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
        driver.setConsent(true);

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

        trip1.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC).plusDays(2));
        trip1.setExpectedStartTime(trip1.getExpectedStartTime().plusDays(1));
        trip1.setPlannedShiftId(shift.getId());

        dslContext.update(Tables.TRIPS_).set(trip1).execute();
        
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("longitude","37.395744")
                        .param("latitude","55.644466")
                        .param("fullSearch", "true")
                        .param("enableShiftFilter", "false")
                        .param("page", "0")
                        .param("size", "20")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {});
        Assertions.assertEquals(1, content.size());
        Assertions.assertEquals(driver.getId().toString(), content.get(0).get("id"));
        Assertions.assertNotNull(content.get(0).get("currentShift"));

        driver.setLongitude(null);
        driver.setLatitude(null);
        dslContext.update(Tables.DRIVER).set(driver).execute();

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("longitude","37.395744")
                        .param("latitude","55.644466")
                        .param("fullSearch", "true")
                        .param("enableShiftFilter", "false")
                        .param("page", "0")
                        .param("size", "20")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {});
        Assertions.assertEquals(1, content.size());
        Assertions.assertEquals(driver.getId().toString(), content.get(0).get("id"));
        Assertions.assertNotNull(content.get(0).get("currentShift"));

        driver.setLatitude(55.644466);
        driver.setLongitude(37.395744);
        driver.setPointTime(OffsetDateTime.now(ZoneOffset.UTC));
        dslContext.update(Tables.DRIVER).set(driver).execute();

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("longitude","37.395744")
                        .param("latitude","55.644466")
                        .param("fullSearch", "false")
                        .param("enableShiftFilter", "true")
                        .param("page", "0")
                        .param("size", "20")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {});
        Assertions.assertEquals(1, content.size());
        Assertions.assertEquals(driver.getId().toString(), content.get(0).get("id"));
        Assertions.assertNotNull(content.get(0).get("currentShift"));

        trip1.setDriverId(driver.getId());
        trip1.setStatus(TripStatus.DRIVER_ASSIGNED.name());
        trip1.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(2));
        dslContext.update(Tables.TRIPS_).set(trip1).execute();

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("longitude","37.395744")
                        .param("latitude","55.644466")
                        .param("fullSearch", "false")
                        .param("enableShiftFilter", "true")
                        .param("page", "0")
                        .param("size", "20")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {});
        Assertions.assertEquals(0, content.size());

        trip1.setDriverId(null);
        trip1.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        dslContext.update(Tables.TRIPS_).set(trip1).execute();

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("longitude","37.395744")
                        .param("latitude","55.644466")
                        .param("fullSearch", "false")
                        .param("enableShiftFilter", "true")
                        .param("tripStartDate", LocalDateTime.now(ZoneOffset.UTC).plusDays(2).toString())
                        .param("tripEndDate", LocalDateTime.now(ZoneOffset.UTC).plusDays(3).toString())
                        .param("page", "0")
                        .param("size", "20")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {});
        Assertions.assertEquals(0, content.size());

        shift.setEndDate(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(2));
        dslContext.update(Tables.SHIFT).set(shift).execute();

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/"+contractor.getId().toString()+"/driver/location/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("longitude","37.395744")
                        .param("latitude","55.644466")
                        .param("fullSearch", "false")
                        .param("enableShiftFilter", "true")
                        .param("page", "0")
                        .param("size", "20")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<LinkedHashMap<String, Object>>>() {});
        Assertions.assertEquals(0, content.size());
    }

    @Test
    void testSetOnline() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/self/online/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DRIVER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
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

        mockMvc.perform(MockMvcRequestBuilders.put("/self/online/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DRIVER").claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
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
    }
}
