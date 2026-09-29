package ru.sber.transport.trips.cargo.controller;

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
import ru.sber.transport.trips.cargo.business.dto.CreateTripResponse;
import ru.sber.transport.trips.cargo.business.dto.GetTripResponse;
import ru.sber.transport.trips.cargo.business.dto.WaypointType;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.dto.IntegrationRequestDTOTest;
import ru.sber.transport.trips.cargo.web.controller.IntegrationController;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.*;

import java.math.BigInteger;
import java.time.Duration;
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
@DisplayName("Проверка контроллера интеграции")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
class IntegrationControllerTest extends KafkaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DSLContext dslContext;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private AuthorizationManager<?> manager;

    private ContractorsRecord contractor;

    private ObjectMapper objectMapper;

    private TripsRecord trip1;

    private DispatcherRecord dispatcher;

    private DriverRecord driver;

    private VehicleRecord vehicle;

    @BeforeEach
    void createData() {
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
        trip1.setStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        trip1.setEndTime(trip1.getStartTime().plusHours(1));
        trip1.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip1.setContractorId(contractor.getId());
        trip1.setWaypoints(JSON.json("[{\"id\":\"5b1aaf8f-4c64-41fa-9efe-30058fab4eef\",\"latitude\":55.787662,\"longitude\":37.509508,\"orderingIndex\":1,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":null,\"address\":\"г.Краснодар, ул.Селезнева, 136\",\"contacts\":[{\"contact\":{\"fullName\":\"Грузовичков Сергей Каргович\",\"phone\":\"+79998887766\"},\"requests\":[{\"humanReadableId\":\"OT-0025-00001281\",\"type\":\"LOAD\",\"organization\":\"ООО Ромашка\",\"cargo\":[{\"orderingIndex\":0,\"cargoName\":\"Коробка\",\"weight\":7.5,\"volume\":90000.0,\"occupiedPlacesCount\":2,\"height\":30.0,\"length\":50.0,\"width\":60.0,\"fragile\":true}],\"loaders\":1,\"comment\":\"Осторожно, не бросать !\",\"pack\":[{\"name\":\"Гофрокороб пятислойный (610мм*400мм*330мм)\",\"unit\":\"штука\",\"count\":2}],\"qrs\":[\"1234\",\"5678\"]}]}]},{\"id\":\"5b1aaf8f-4c64-41fa-9efe-30058fab4eef\",\"latitude\":55.787662,\"longitude\":37.509508,\"orderingIndex\":1,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":null,\"address\":\"г.Краснодар, ул.Селезнева, 136\",\"contacts\":[{\"contact\":{\"fullName\":\"Грузовичков Сергей Каргович\",\"phone\":\"+79998887766\"},\"requests\":[{\"humanReadableId\":\"OT-0025-00001283\",\"type\":null,\"organization\":\"ООО Ромашка\",\"cargo\":[{\"orderingIndex\":0,\"cargoName\":\"Документы ЦАРС\",\"weight\":7.5,\"volume\":90000.0,\"occupiedPlacesCount\":2,\"height\":30.0,\"length\":50.0,\"width\":60.0,\"fragile\":true}],\"loaders\":1,\"comment\":\"Осторожно, не бросать !\",\"pack\":[{\"name\":\"Гофрокороб пятислойный (610мм*400мм*330мм)\",\"unit\":\"штука\",\"count\":2}],\"qrs\":[\"4321\",\"8765\"]}]}]}]"));
        trip1.setRequests(JSON.json("[{\"id\":\"6f629b1e-a6c5-415b-8043-d7c62f5ccb5b\",\"humanReadableId\":\"OT-0033-00106240\",\"author\":{\"id\":\"cb5ede52-669f-40ff-8dc1-c232482be415\",\"lastName\":\"Рассохин\",\"firstName\":\"Евгений\",\"patronymic\":\"Викторович\",\"mobilePhone\":\"+79819519519\",\"email\":null,\"organization\":null},\"sender\":{\"id\":\"fbea0c83-9fd9-46af-a8e4-2c0db4f5b065\",\"lastName\":\"Ахмедова\",\"firstName\":\"Виолетта\",\"patronymic\":\"Сергеевна\",\"mobilePhone\":null,\"email\":null,\"organization\":\"Доп.офис №9040/00828\"},\"recipient\":{\"id\":\"cb5ede52-669f-40ff-8dc1-c232482be415\",\"lastName\":\"Рассохин\",\"firstName\":\"Евгений\",\"patronymic\":\"Викторович\",\"mobilePhone\":null,\"email\":null,\"organization\":\"Управление сопровождения территориально-зависимого производства \"},\"creationTime\":\"2023-01-27T07:28:48.000000884+00:00\",\"approvalDate\":\"2023-01-27T07:28:46.000000951+00:00\",\"desiredDate\":\"2023-12-26T00:00:00+00:00\",\"timeZone\":\"+3\",\"transportType\":\"DEDICATED\",\"approvedBy\":{\"id\":\"cb5ede52-669f-40ff-8dc1-c232482be415\",\"lastName\":\"Рассохин\",\"firstName\":\"Евгений\",\"patronymic\":\"Викторович\",\"mobilePhone\":\"+79819519519\",\"email\":null,\"organization\":null},\"tariffId\":\"5c3e800a-9833-4898-991a-1bcf3d1c588d\",\"status\":\"CARGO_APPROVED\",\"statusCode\":0,\"approvalState\":\"AWAITING_APPROVAL\",\"expected\":{\"cost\":4.6771896E7,\"deliveryTime\":0,\"distance\":1826.91,\"time\":96043.000000000},\"requestOptions\":[],\"active\":true,\"length\":3.0,\"width\":21.0,\"height\":29.5,\"volume\":1890.0,\"weight\":1.0,\"occupiedPlacesCount\":1,\"cargoData\":[{\"id\":\"6556a22b-a78d-40d9-a43e-55bc38b6a374\",\"position\":1,\"name\":\"Пакет\",\"type\":\"другое\",\"category\":\"другое\",\"length\":3.0,\"width\":21.0,\"height\":29.5,\"volume\":1890.0,\"weight\":1.0,\"occupiedPlacesCount\":1,\"fragile\":false,\"needPackage\":false,\"packageId\":null,\"packageCount\":0}],\"comment\":\"ТЕСТ Дополнительная информация\",\"finishedTime\":null,\"transferTime\":null,\"shipmentTime\":null,\"timeWorkStart\":null,\"sourceLoaders\":1,\"destinationLoaders\":1,\"timeWorkFinish\":null}]"));
        trip1.setDigitId(BigInteger.ONE);
        trip1.setFactDistance(10.02);
        trip1.setCapacity(1.0);
        trip1.setLoadersWorkTime(23000L);
        trip1.setDispatcherId(dispatcher.getId());
        trip1.setRouteHumanReadableId("CT-0001-00000001");

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
        dslContext.insertInto(Tables.TRIPS).set(trip1).execute();
    }

    @Test
    void testCreateTrip() throws Exception {
        var integrationRequestDTO = Instancio.of(IntegrationRequestDTOTest.class)
                .set(Select.field(IntegrationRequestDTOTest::getHumanReadableId), "CT-0025-00001299")
                .set(Select.field(IntegrationRequestDTOTest::getAuthor), new IntegrationRequestDTOTest.ContactDto(
                        Instancio.create(String.class),
                        Instancio.create(String.class)
                ))
                .set(Select.field(IntegrationRequestDTOTest::getDesiredDate), OffsetDateTime.now().plusHours(1))
                .set(Select.field(IntegrationRequestDTOTest::getDesiredAuto), new IntegrationRequestDTOTest.Auto(
                        Instancio.create(Double.class),
                        Instancio.create(Double.class)
                ))
                .set(Select.field(IntegrationRequestDTOTest::getWaypoints), List.of(generateWaypoint()))
        .create();
        var body = objectMapper.writeValueAsString(List.of(integrationRequestDTO));
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/integration")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<List<CreateTripResponse>>() {});
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.get(0).isSuccess());
        Assertions.assertEquals(integrationRequestDTO.getHumanReadableId(), result.get(0).orderSbertransportId());
        Assertions.assertNotNull(result.get(0).orderParthnerId());
    }

    @Test
    void testCreateTripIntegrationClient() throws Exception {
        var integrationClientId = UUID.randomUUID();
        dslContext.insertInto(Tables.INTEGRATION_CLIENT)
                .set(new IntegrationClientRecord(integrationClientId, contractor.getId(), true))
                .execute();
        var integrationRequestDTO = Instancio.of(IntegrationRequestDTOTest.class)
                .set(Select.field(IntegrationRequestDTOTest::getHumanReadableId), "CT-0025-00001299")
                .set(Select.field(IntegrationRequestDTOTest::getAuthor), new IntegrationRequestDTOTest.ContactDto(
                        Instancio.create(String.class),
                        Instancio.create(String.class)
                ))
                .set(Select.field(IntegrationRequestDTOTest::getDesiredDate), OffsetDateTime.now().plusHours(1))
                .set(Select.field(IntegrationRequestDTOTest::getDesiredAuto), new IntegrationRequestDTOTest.Auto(
                        Instancio.create(Double.class),
                        Instancio.create(Double.class)
                ))
                .set(Select.field(IntegrationRequestDTOTest::getWaypoints), List.of(generateWaypoint()))
                .create();
        var body = objectMapper.writeValueAsString(List.of(integrationRequestDTO));
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/integration")
                        .with(jwt().jwt(builder -> builder.jti(integrationClientId.toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<List<CreateTripResponse>>() {});
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.get(0).isSuccess());
        Assertions.assertEquals(integrationRequestDTO.getHumanReadableId(), result.get(0).orderSbertransportId());
        Assertions.assertNotNull(result.get(0).orderParthnerId());
    }

    @Test
    void testCreateTripUnauthorized() throws Exception {
        var integrationRequestDTO = Instancio.of(IntegrationRequestDTOTest.class)
                .set(Select.field(IntegrationRequestDTOTest::getHumanReadableId), "CT-0025-00001299")
                .set(Select.field(IntegrationRequestDTOTest::getAuthor), new IntegrationRequestDTOTest.ContactDto(
                        Instancio.create(String.class),
                        Instancio.create(String.class)
                ))
                .set(Select.field(IntegrationRequestDTOTest::getDesiredDate), OffsetDateTime.now().plusHours(1))
                .set(Select.field(IntegrationRequestDTOTest::getDesiredAuto), new IntegrationRequestDTOTest.Auto(
                        Instancio.create(Double.class),
                        Instancio.create(Double.class)
                ))
                .set(Select.field(IntegrationRequestDTOTest::getWaypoints), List.of(generateWaypoint()))
                .create();
        var body = objectMapper.writeValueAsString(List.of(integrationRequestDTO));
        mockMvc.perform(MockMvcRequestBuilders.post("/integration")
                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testCreateTripConflict() throws Exception {
        var integrationRequestDTO = Instancio.of(IntegrationRequestDTOTest.class)
                .set(Select.field(IntegrationRequestDTOTest::getHumanReadableId), "CT-0025-00001299")
                .set(Select.field(IntegrationRequestDTOTest::getAuthor), new IntegrationRequestDTOTest.ContactDto(
                        Instancio.create(String.class),
                        Instancio.create(String.class)
                ))
                .set(Select.field(IntegrationRequestDTOTest::getDesiredDate), OffsetDateTime.now().plusHours(1))
                .set(Select.field(IntegrationRequestDTOTest::getDesiredAuto), new IntegrationRequestDTOTest.Auto(
                        Instancio.create(Double.class),
                        Instancio.create(Double.class)
                ))
                .set(Select.field(IntegrationRequestDTOTest::getWaypoints), List.of(generateWaypoint()))
                .create();
        var body = objectMapper.writeValueAsString(List.of(integrationRequestDTO));
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
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<List<CreateTripResponse>>() {});
        Assertions.assertEquals(1, result.size());
        Assertions.assertFalse(result.get(0).isSuccess());
        Assertions.assertNotNull(result.get(0).error());
        Assertions.assertEquals(409, result.get(0).error().status());
        Assertions.assertEquals("Route CT-0025-00001299 already exists", result.get(0).error().message());
    }

    @Test
    void testGetTrip() throws Exception {
        trip1 = dslContext.selectFrom(Tables.TRIPS).where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();
        trip1.setStatus("ORDER_FINISHED");
        dslContext.update(Tables.TRIPS).set(trip1).execute();
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/integration/TC-0001-00000001")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), GetTripResponse.class);
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertNotNull(result.order());
        Assertions.assertFalse(result.order().requests().isEmpty());
        Assertions.assertEquals(2, result.order().requests().size());
        Assertions.assertEquals(2, result.order().requests().get(0).qrs().size());
        Assertions.assertNull(result.error());
    }

    @Test
    void testGetTripUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/integration/TC-0001-00000001")
                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testGetTripNotFound() throws Exception {
        var response =mockMvc.perform(MockMvcRequestBuilders.get("/integration/TC-0001-00000021")
                .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
        ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), GetTripResponse.class);
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertNull(result.order());
        Assertions.assertNotNull(result.error());
        Assertions.assertEquals(404, result.error().status());
        Assertions.assertEquals("Route TC-0001-00000021 not found", result.error().message());
    }

    @Test
    void testCancelTrip() throws Exception {
        trip1 = dslContext.selectFrom(Tables.TRIPS).where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();
        trip1.setStatus("WAITING_FOR_ASSIGNMENT");
        dslContext.update(Tables.TRIPS).set(trip1).execute();
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/integration/TC-0001-00000001/cancel")
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
        trip1 = dslContext.selectFrom(Tables.TRIPS).where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();
        trip1.setStatus("WAITING_FOR_ASSIGNMENT");
        dslContext.update(Tables.TRIPS).set(trip1).execute();
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/integration/TC-0001-00000021/cancel")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), CreateTripResponse.class);
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertNotNull(result.error());
        Assertions.assertEquals(404, result.error().status());
        Assertions.assertEquals("Route TC-0001-00000021 not found", result.error().message());
    }

    @Test
    void testCancelTripConflict() throws Exception {
        trip1 = dslContext.selectFrom(Tables.TRIPS).where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();
        trip1.setStatus("ORDER_FINISHED");
        dslContext.update(Tables.TRIPS).set(trip1).execute();
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/integration/TC-0001-00000001/cancel")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("")
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), CreateTripResponse.class);
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertNotNull(result.error());
        Assertions.assertEquals(409, result.error().status());
        Assertions.assertEquals("Trip TC-0001-00000001 is already finished, cancel is impossible", result.error().message());
    }

    private IntegrationRequestDTOTest.RouteWaypointDto generateWaypoint() {
        return new IntegrationRequestDTOTest.RouteWaypointDto(
                Instancio.create(Integer.class),
                UUID.randomUUID(),
                Instancio.create(WaypointType.class),
                new IntegrationRequestDTOTest.Address(
                        Instancio.create(String.class),
                        new IntegrationRequestDTOTest.Coordinates(
                                Instancio.create(Double.class),
                                Instancio.create(Double.class)
                        )
                ),
                List.of(new IntegrationRequestDTOTest.Contact(
                        new IntegrationRequestDTOTest.ContactDto(
                                Instancio.create(String.class),
                                Instancio.create(String.class)
                        ),
                        List.of(new IntegrationRequestDTOTest.RouteRequestDto(
                                "OT-0025-00001299",
                                Instancio.create(WaypointType.class),
                                Instancio.create(String.class),
                                List.of(new IntegrationRequestDTOTest.CargoData(
                                        Instancio.create(Integer.class),
                                        Instancio.create(String.class),
                                        Instancio.create(Double.class),
                                        Instancio.create(Double.class),
                                        Instancio.create(Integer.class),
                                        Instancio.create(Double.class),
                                        Instancio.create(Double.class),
                                        Instancio.create(Double.class),
                                        Instancio.create(Boolean.class)
                                )),
                                Instancio.create(Integer.class),
                                Instancio.create(String.class),
                                List.of(new IntegrationRequestDTOTest.Pack(
                                        Instancio.create(String.class),
                                        Instancio.create(String.class),
                                        Instancio.create(Integer.class)
                                ))
                        ))

                ))
        );
    }

}
