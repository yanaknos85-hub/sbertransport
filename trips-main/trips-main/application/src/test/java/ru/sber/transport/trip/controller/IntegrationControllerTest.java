package ru.sber.transport.trip.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
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
import ru.sber.transport.trip.business.dto.*;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.*;
import ru.sber.transport.trip.dto.IntegrationRequestDTOTest;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.math.BigInteger;
import java.time.*;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера интеграции")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
@ActiveProfiles("test")
class IntegrationControllerTest extends KafkaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DSLContext dslContext;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private AuthorizationManager<?> manager;

    private ContractorsRecord contractor;

    private ObjectMapper objectMapper;

    private TripsRecord trip1;

    private DispatcherRecord dispatcher;

    private DriverRecord driver;

    private VehicleRecord vehicle;

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
        trip1.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        trip1.setFactEndTime(trip1.getFactStartTime().plusHours(1));
        trip1.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
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
        trip1.setFactCost(9999L);

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
        dslContext.update(Tables.DRIVER).set(driver).execute();

        trip1.setDriverId(driver.getId());
        trip1.setVehicleId(vehicle.getId());
        trip1.setStatus(TripStatus.DRIVER_ASSIGNED.name());
        dslContext.insertInto(Tables.TRIPS_).set(trip1).execute();
    }

    @Test
    void testCreateTrip() throws Exception {
        var integrationRequestDTO = Instancio.of(IntegrationRequestDTOTest.class)
                .set(Select.field(IntegrationRequestDTOTest::getRequestId), UUID.randomUUID())
                .set(Select.field(IntegrationRequestDTOTest::getPlanStartTime), OffsetDateTime.now().plusHours(1))
                .set(Select.field(IntegrationRequestDTOTest::getRoutePoints), generateWaypoint())
                .set(Select.field(IntegrationRequestDTOTest::getTripClass), "BUSINESS")
                .set(Select.field(IntegrationRequestDTOTest::getComment), "COMMENT")
                .set(Select.field(IntegrationRequestDTOTest::getHumanReadableId), "TT-OT-0001-00063331")
                .create();
        var body = objectMapper.writeValueAsString(integrationRequestDTO);
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/integration")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<CreateTripResponse>() {});
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(integrationRequestDTO.getRequestId().toString(), result.orderSbertransportId());
        Assertions.assertNotNull(result.orderParthnerId());
    }

    @Test
    void testCreateTripIntegrationClient() throws Exception {
        var integrationClientId = UUID.randomUUID();
        dslContext.insertInto(Tables.INTEGRATION_CLIENT)
                .set(new IntegrationClientRecord(integrationClientId, contractor.getId(), true))
                .execute();
        var integrationRequestDTO = Instancio.of(IntegrationRequestDTOTest.class)
                .set(Select.field(IntegrationRequestDTOTest::getRequestId), UUID.randomUUID())
                .set(Select.field(IntegrationRequestDTOTest::getPlanStartTime), OffsetDateTime.now().plusHours(1))
                .set(Select.field(IntegrationRequestDTOTest::getRoutePoints), generateWaypoint())
                .set(Select.field(IntegrationRequestDTOTest::getTripClass), "BUSINESS")
                .set(Select.field(IntegrationRequestDTOTest::getComment), "COMMENT")
                .set(Select.field(IntegrationRequestDTOTest::getHumanReadableId), "TT-OT-0001-00063331")
                .create();
        var body = objectMapper.writeValueAsString(integrationRequestDTO);
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/integration")
                        .with(jwt().jwt(builder -> builder.jti(integrationClientId.toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<CreateTripResponse>() {});
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(integrationRequestDTO.getRequestId().toString(), result.orderSbertransportId());
        Assertions.assertNotNull(result.orderParthnerId());
    }

    @Test
    void testCreateTripAlreadyExists() throws Exception {
        var integrationRequestDTO = Instancio.of(IntegrationRequestDTOTest.class)
                .set(Select.field(IntegrationRequestDTOTest::getRequestId), UUID.randomUUID())
                .set(Select.field(IntegrationRequestDTOTest::getPlanStartTime), OffsetDateTime.now().plusHours(1))
                .set(Select.field(IntegrationRequestDTOTest::getRoutePoints), generateWaypoint())
                .set(Select.field(IntegrationRequestDTOTest::getTripClass), "BUSINESS")
                .set(Select.field(IntegrationRequestDTOTest::getComment), "COMMENT")
                .set(Select.field(IntegrationRequestDTOTest::getHumanReadableId), "TT-OT-0001-00063331")
                .create();
        var body = objectMapper.writeValueAsString(integrationRequestDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/integration")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                ).andExpect(MockMvcResultMatchers.status().isOk());
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/integration")
                .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<CreateTripResponse>() {});
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertNotNull(result.error());
        Assertions.assertEquals(409, result.error().status());
        Assertions.assertEquals("Trip " + integrationRequestDTO.getHumanReadableId() + " already exists", result.error().message());
    }

    @Test
    void testCreateTripUnauthorized() throws Exception {
        var integrationRequestDTO = Instancio.of(IntegrationRequestDTOTest.class)
                .set(Select.field(IntegrationRequestDTOTest::getRequestId), UUID.randomUUID())
                .set(Select.field(IntegrationRequestDTOTest::getPlanStartTime), OffsetDateTime.now().plusHours(1))
                .set(Select.field(IntegrationRequestDTOTest::getRoutePoints), generateWaypoint())
                .set(Select.field(IntegrationRequestDTOTest::getTripClass), "BUSINESS")
                .set(Select.field(IntegrationRequestDTOTest::getComment), "COMMENT")
                .set(Select.field(IntegrationRequestDTOTest::getHumanReadableId), "TT-OT-0001-00063331")
                .create();
        var body = objectMapper.writeValueAsString(integrationRequestDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/integration")
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                ).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testGetTrip() throws Exception {
        trip1 = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        trip1.setStatus("ORDER_FINISHED");
        dslContext.update(Tables.TRIPS_).set(trip1).execute();
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/integration/TP-0001-00000001")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), GetTripResponse.class);
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertNotNull(result.order());
        Assertions.assertNull(result.error());
        Assertions.assertEquals(trip1.getFactCost(), result.order().getPrice());
        Assertions.assertEquals(3600, result.order().getWaitTimeOW());
    }

    @Test
    void testGetTripNotFound() throws Exception {
        trip1 = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        trip1.setStatus("ORDER_FINISHED");
        dslContext.update(Tables.TRIPS_).set(trip1).execute();
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/integration/TP-0001-0000021")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), GetTripResponse.class);
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertNull(result.order());
        Assertions.assertNotNull(result.error());
        Assertions.assertEquals(404, result.error().status());
        Assertions.assertEquals("Trip TP-0001-0000021 not found", result.error().message());
    }

    @Test
    void testGetTripUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/integration/"+trip1.getId())
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testCancelTrip() throws Exception {
        trip1 = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        trip1.setStatus("WAITING_FOR_ASSIGNMENT");
        dslContext.update(Tables.TRIPS_).set(trip1).execute();
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/integration/TP-0001-00000001/cancel")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), CreateTripResponse.class);
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertNotNull(result.orderParthnerId());
        Assertions.assertNotNull(result.orderSbertransportId());
        Assertions.assertNull(result.error());
    }

    @Test
    void testCancelTripNotFound() throws Exception {
        trip1 = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        trip1.setStatus("WAITING_FOR_ASSIGNMENT");
        dslContext.update(Tables.TRIPS_).set(trip1).execute();
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/integration/TP-0001-00000021/cancel")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), CreateTripResponse.class);
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertNull(result.orderParthnerId());
        Assertions.assertNull(result.orderSbertransportId());
        Assertions.assertNotNull(result.error());
        Assertions.assertEquals(404, result.error().status());
        Assertions.assertEquals("Trip TP-0001-00000021 not found", result.error().message());
    }

    @Test
    void testCancelTripConflict() throws Exception {
        trip1 = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchOne();
        trip1.setStatus("ORDER_FINISHED");
        dslContext.update(Tables.TRIPS_).set(trip1).execute();
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/integration/TP-0001-00000001/cancel")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), CreateTripResponse.class);
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertNull(result.orderParthnerId());
        Assertions.assertNull(result.orderSbertransportId());
        Assertions.assertNotNull(result.error());
        Assertions.assertEquals(409, result.error().status());
        Assertions.assertEquals("Trip TP-0001-00000001 is already finished, cancel is impossible", result.error().message());
    }

    private IntegrationRequestDTOTest.OrderRoutePoints generateWaypoint() {
        var source = new IntegrationRequestDTOTest.Waypoint(
                "Address 1",
                50.00001,
                52.00001,
                0L,
                List.of(new IntegrationRequestDTOTest.Contact(
                        "BOARDING",
                        "FirstName",
                        "Patronymic",
                        "+70374483301"
                ))
        );
        var dest = new IntegrationRequestDTOTest.Waypoint(
                "Address 2",
                50.00021,
                52.00011,
                320L,
                List.of(new IntegrationRequestDTOTest.Contact(
                        "UNBOARDING",
                        "FirstName",
                        "Patronymic",
                        "+70374483301"
                ), new IntegrationRequestDTOTest.Contact(
                        "BOARDING",
                        "FirstName2",
                        "Patronymic2",
                        "+70374483302"
                ))
        );
        var intermediate = new IntegrationRequestDTOTest.Waypoint(
                "Address 3",
                50.00025,
                52.00016,
                0L,
                List.of(new IntegrationRequestDTOTest.Contact(
                        "UNBOARDING",
                        "FirstName2",
                        "Patronymic2",
                        "+70374483302"
                ))
        );
        return new IntegrationRequestDTOTest.OrderRoutePoints(null, null, List.of(source, intermediate, dest));
    }

}
