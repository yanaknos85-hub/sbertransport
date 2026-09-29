package ru.sber.transport.trips.cargo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.dispatcher.messages.ContractorUpdateTripMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.trips.cargo.business.dto.TripV2Dto;
import ru.sber.transport.trips.cargo.business.dto.v2.CheckinResponseDtoV2;
import ru.sber.transport.trips.cargo.business.model.*;
import ru.sber.transport.trips.cargo.messaging.ChannelType;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.messaging.providers.DispatcherProvider;
import ru.sber.transport.trips.cargo.messaging.providers.DriverProvider;
import ru.sber.transport.trips.cargo.messaging.providers.VehicleProvider;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.TripSender;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.impl.TripDispatcherSender;
import ru.sber.transport.trips.cargo.providers.trips.mapping.TripMapper;
import ru.sber.transport.trips.cargo.providers.trips.mapping.TripMapperImpl;
import ru.sber.transport.trips.cargo.providers.trips.mapping.WaypointsMapper;
import ru.sber.transport.trips.cargo.providers.trips.mapping.WaypointsMapperImpl;
import ru.sber.transport.trips.cargo.web.exceptions.ConflictException;
import ru.sber.transport.trips.cargo.web.service.VerificationService;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.*;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
class TripControllerTest extends KafkaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private VerificationService verificationService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private AuthorizationManager<?> manager;

    private final OutputBridge outputBridge = mock(OutputBridge.class);

    private final OutputBridge outputSslBridge = mock(OutputBridge.class);

    private final ContractorProvider contractorProvider = mock(ContractorProvider.class);

    private final DispatcherProvider dispatcherProvider = mock(DispatcherProvider.class);

    private final DriverProvider driverProvider = mock(DriverProvider.class);

    private final VehicleProvider vehicleProvider = mock(VehicleProvider.class);

    private final WaypointsMapper waypointsMapper = new WaypointsMapperImpl();

    private final TripMapper messageTripMapper = new TripMapperImpl(waypointsMapper) {
        @Override
        public ObjectMapper objectMapper() {
            return objectMapper;
        }
    };

    private final ObjectProvider<OutputBridge> bridge = new ObjectProvider<>() {
        @Override
        public OutputBridge getObject(Object... args) throws BeansException {
            return outputBridge;
        }

        @Override
        public OutputBridge getIfAvailable() throws BeansException {
            return outputBridge;
        }

        @Override
        public OutputBridge getIfUnique() throws BeansException {
            return outputBridge;
        }

        @Override
        public OutputBridge getObject() throws BeansException {
            return outputBridge;
        }
    };

    private final ObjectProvider<OutputBridge> bridgeSsl = new ObjectProvider<>() {
        @Override
        public OutputBridge getObject(Object... args) throws BeansException {
            return outputSslBridge;
        }

        @Override
        public OutputBridge getIfAvailable() throws BeansException {
            return outputSslBridge;
        }

        @Override
        public OutputBridge getIfUnique() throws BeansException {
            return outputSslBridge;
        }

        @Override
        public OutputBridge getObject() throws BeansException {
            return outputSslBridge;
        }
    };

    private final TripSender tripDispatcherSender = new TripDispatcherSender(bridge, bridgeSsl, contractorProvider, dispatcherProvider, driverProvider, vehicleProvider, messageTripMapper);

    private ContractorsRecord contractor;

    private DispatcherRecord dispatcher;

    private DriverRecord driver;

    private TripsRecord trip1;

    private TripsRecord trip2;

    private TripsRecord trip3;

    private ShiftRecord shift;

    private AutoparkRecord autopark;

    private ObjectMapper objectMapper;

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
        trip1.setWaypoints(JSON.json("[{\"id\":\"5b1aaf8f-4c64-41fa-9efe-30058fab4eef\",\"latitude\":55.787662,\"longitude\":37.509508,\"orderingIndex\":1,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":null,\"address\":\"г.Краснодар, ул.Селезнева, 136\",\"contacts\":[{\"contact\":{\"fullName\":\"Грузовичков Сергей Каргович\",\"phone\":\"+79998887766\"},\"requests\":[{\"humanReadableId\":\"OT-0025-00001281\",\"type\":\"LOAD\",\"organization\":\"ООО Ромашка\",\"cargo\":[{\"orderingIndex\":0,\"cargoName\":\"Коробка\",\"weight\":7.5,\"volume\":90000.0,\"occupiedPlacesCount\":2,\"height\":30.0,\"length\":50.0,\"width\":60.0,\"fragile\":true}],\"loaders\":1,\"comment\":\"Осторожно, не бросать !\",\"pack\":[{\"name\":\"Гофрокороб пятислойный (610мм*400мм*330мм)\",\"unit\":\"штука\",\"count\":2}],\"qrs\":[]}]}]},{\"id\":\"5b1aaf8f-4c64-41fa-9efe-30058fab4eef\",\"latitude\":55.787662,\"longitude\":37.509508,\"orderingIndex\":1,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":null,\"address\":\"г.Краснодар, ул.Селезнева, 136\",\"contacts\":[{\"contact\":{\"fullName\":\"Грузовичков Сергей Каргович\",\"phone\":\"+79998887766\"},\"requests\":[{\"humanReadableId\":\"OT-0025-00001283\",\"type\":null,\"organization\":\"ООО Ромашка\",\"cargo\":[{\"orderingIndex\":0,\"cargoName\":\"Документы ЦАРС\",\"weight\":7.5,\"volume\":90000.0,\"occupiedPlacesCount\":2,\"height\":30.0,\"length\":50.0,\"width\":60.0,\"fragile\":true}],\"loaders\":1,\"comment\":\"Осторожно, не бросать !\",\"pack\":[{\"name\":\"Гофрокороб пятислойный (610мм*400мм*330мм)\",\"unit\":\"штука\",\"count\":2}],\"qrs\":[]}]}]}]"));
        trip1.setRequests(JSON.json("[{\"id\":\"6f629b1e-a6c5-415b-8043-d7c62f5ccb5b\",\"humanReadableId\":\"OT-0033-00106240\",\"author\":{\"id\":\"cb5ede52-669f-40ff-8dc1-c232482be415\",\"lastName\":\"Рассохин\",\"firstName\":\"Евгений\",\"patronymic\":\"Викторович\",\"mobilePhone\":\"+79819519519\",\"email\":null,\"organization\":null},\"sender\":{\"id\":\"fbea0c83-9fd9-46af-a8e4-2c0db4f5b065\",\"lastName\":\"Ахмедова\",\"firstName\":\"Виолетта\",\"patronymic\":\"Сергеевна\",\"mobilePhone\":null,\"email\":null,\"organization\":\"Доп.офис №9040/00828\"},\"recipient\":{\"id\":\"cb5ede52-669f-40ff-8dc1-c232482be415\",\"lastName\":\"Рассохин\",\"firstName\":\"Евгений\",\"patronymic\":\"Викторович\",\"mobilePhone\":null,\"email\":null,\"organization\":\"Управление сопровождения территориально-зависимого производства \"},\"creationTime\":\"2023-01-27T07:28:48.000000884+00:00\",\"approvalDate\":\"2023-01-27T07:28:46.000000951+00:00\",\"desiredDate\":\"2023-12-26T00:00:00+00:00\",\"timeZone\":\"+3\",\"transportType\":\"DEDICATED\",\"approvedBy\":{\"id\":\"cb5ede52-669f-40ff-8dc1-c232482be415\",\"lastName\":\"Рассохин\",\"firstName\":\"Евгений\",\"patronymic\":\"Викторович\",\"mobilePhone\":\"+79819519519\",\"email\":null,\"organization\":null},\"tariffId\":\"5c3e800a-9833-4898-991a-1bcf3d1c588d\",\"status\":\"CARGO_APPROVED\",\"statusCode\":0,\"approvalState\":\"AWAITING_APPROVAL\",\"expected\":{\"cost\":4.6771896E7,\"deliveryTime\":0,\"distance\":1826.91,\"time\":96043.000000000},\"requestOptions\":[],\"active\":true,\"length\":3.0,\"width\":21.0,\"height\":29.5,\"volume\":1890.0,\"weight\":1.0,\"occupiedPlacesCount\":1,\"cargoData\":[{\"id\":\"6556a22b-a78d-40d9-a43e-55bc38b6a374\",\"position\":1,\"name\":\"Пакет\",\"type\":\"другое\",\"category\":\"другое\",\"length\":3.0,\"width\":21.0,\"height\":29.5,\"volume\":1890.0,\"weight\":1.0,\"occupiedPlacesCount\":1,\"fragile\":false,\"needPackage\":false,\"packageId\":null,\"packageCount\":0}],\"comment\":\"ТЕСТ Дополнительная информация\",\"finishedTime\":null,\"transferTime\":null,\"shipmentTime\":null,\"timeWorkStart\":null,\"sourceLoaders\":1,\"destinationLoaders\":1,\"timeWorkFinish\":null}]"));
        trip1.setDigitId(BigInteger.ONE);
        trip1.setFactDistance(10.02);
        trip1.setCapacity(1.0);
        trip1.setLoadersWorkTime(23000L);
        trip1.setDispatcherId(dispatcher.getId());
        trip1.setRouteHumanReadableId("CT-0001-00000001");
        trip1.setLoaders(1);

        trip2 = new TripsRecord();
        trip2.setId(UUID.randomUUID());
        trip2.setStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        trip2.setEndTime(trip2.getStartTime().plusHours(1));
        trip2.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip2.setContractorId(contractor.getId());
        trip2.setWaypoints(JSON.json("[{\"id\":\"5b1aaf8f-4c64-41fa-9efe-30058fab4eef\",\"latitude\":55.787662,\"longitude\":37.509508,\"orderingIndex\":1,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":null,\"address\":\"г.Краснодар, ул.Селезнева, 136\",\"contacts\":[{\"contact\":{\"fullName\":\"Грузовичков Сергей Каргович\",\"phone\":\"+79998887766\"},\"requests\":[{\"humanReadableId\":\"OT-0025-00001281\",\"type\":\"LOAD\",\"organization\":\"ООО Ромашка\",\"cargo\":[{\"orderingIndex\":0,\"cargoName\":\"Коробка\",\"weight\":7.5,\"volume\":90000.0,\"occupiedPlacesCount\":2,\"height\":30.0,\"length\":50.0,\"width\":60.0,\"fragile\":true}],\"loaders\":1,\"comment\":\"Осторожно, не бросать !\",\"pack\":[{\"name\":\"Гофрокороб пятислойный (610мм*400мм*330мм)\",\"unit\":\"штука\",\"count\":2}],\"qrs\":[]}]}]},{\"id\":\"5b1aaf8f-4c64-41fa-9efe-30058fab4eef\",\"latitude\":55.787662,\"longitude\":37.509508,\"orderingIndex\":1,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":null,\"address\":\"г.Краснодар, ул.Селезнева, 136\",\"contacts\":[{\"contact\":{\"fullName\":\"Грузовичков Сергей Каргович\",\"phone\":\"+79998887766\"},\"requests\":[{\"humanReadableId\":\"OT-0025-00001283\",\"type\":null,\"organization\":\"ООО Ромашка\",\"cargo\":[{\"orderingIndex\":0,\"cargoName\":\"Документы ЦАРС\",\"weight\":7.5,\"volume\":90000.0,\"occupiedPlacesCount\":2,\"height\":30.0,\"length\":50.0,\"width\":60.0,\"fragile\":true}],\"loaders\":1,\"comment\":\"Осторожно, не бросать !\",\"pack\":[{\"name\":\"Гофрокороб пятислойный (610мм*400мм*330мм)\",\"unit\":\"штука\",\"count\":2}],\"qrs\":[]}]}]}]"));
        trip2.setRequests(JSON.json("[]"));
        trip2.setDigitId(BigInteger.TWO);
        trip2.setFactDistance(10.02);
        trip1.setCapacity(1.0);
        trip1.setLoadersWorkTime(23000L);
        trip2.setDispatcherId(dispatcher.getId());
        trip2.setRouteHumanReadableId("CT-0001-00000002");
        trip2.setLoaders(2);

        trip3 = new TripsRecord();
        trip3.setId(UUID.randomUUID());
        trip3.setStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        trip3.setEndTime(trip3.getStartTime().plusHours(1));
        trip3.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip3.setContractorId(contractor.getId());
        trip3.setWaypoints(JSON.json("[{\"id\":\"5b1aaf8f-4c64-41fa-9efe-30058fab4eef\",\"latitude\":55.787662,\"longitude\":37.509508,\"orderingIndex\":1,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":null,\"address\":\"г.Краснодар, ул.Селезнева, 136\",\"contacts\":[{\"contact\":{\"fullName\":\"Грузовичков Сергей Каргович\",\"phone\":\"+79998887766\"},\"requests\":[{\"humanReadableId\":\"OT-0025-00001281\",\"type\":\"LOAD\",\"organization\":\"ООО Ромашка\",\"cargo\":[{\"orderingIndex\":0,\"cargoName\":\"Коробка\",\"weight\":7.5,\"volume\":90000.0,\"occupiedPlacesCount\":2,\"height\":30.0,\"length\":50.0,\"width\":60.0,\"fragile\":true}],\"loaders\":1,\"comment\":\"Осторожно, не бросать !\",\"pack\":[{\"name\":\"Гофрокороб пятислойный (610мм*400мм*330мм)\",\"unit\":\"штука\",\"count\":2}],\"qrs\":[]}]}]},{\"id\":\"5b1aaf8f-4c64-41fa-9efe-30058fab4eef\",\"latitude\":55.787662,\"longitude\":37.509508,\"orderingIndex\":1,\"country\":null,\"region\":null,\"city\":null,\"street\":null,\"house\":null,\"building\":null,\"waitingTime\":null,\"address\":\"г.Краснодар, ул.Селезнева, 136\",\"contacts\":[{\"contact\":{\"fullName\":\"Грузовичков Сергей Каргович\",\"phone\":\"+79998887766\"},\"requests\":[{\"humanReadableId\":\"OT-0025-00001283\",\"type\":null,\"organization\":\"ООО Ромашка\",\"cargo\":[{\"orderingIndex\":0,\"cargoName\":\"Документы ЦАРС\",\"weight\":7.5,\"volume\":90000.0,\"occupiedPlacesCount\":2,\"height\":30.0,\"length\":50.0,\"width\":60.0,\"fragile\":true}],\"loaders\":1,\"comment\":\"Осторожно, не бросать !\",\"pack\":[{\"name\":\"Гофрокороб пятислойный (610мм*400мм*330мм)\",\"unit\":\"штука\",\"count\":2}],\"qrs\":[]}]}]}]"));
        trip3.setRequests(JSON.json("[]"));
        trip3.setDigitId(BigInteger.valueOf(3L));
        trip3.setFactDistance(10.02);
        trip3.setCapacity(1.0);
        trip3.setLoadersWorkTime(23000L);
        trip3.setRouteHumanReadableId("CT-0001-00000003");
        trip3.setLoaders(3);

        dslContext.insertInto(Tables.TRIPS).set(trip1).execute();
        dslContext.insertInto(Tables.TRIPS).set(trip2).execute();
        dslContext.insertInto(Tables.TRIPS).set(trip3).execute();

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

        autopark = new AutoparkRecord();
        autopark.setId(UUID.randomUUID());
        autopark.setContractorId(contractor.getId());
        autopark.setRoutingId(UUID.randomUUID());
        autopark.setActive(true);

        dslContext.insertInto(Tables.AUTOPARK).set(autopark).execute();

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
    void getAllTest() throws Exception {
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/")
                        .with(buildDispatherJwt())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(),
                new TypeReference<LinkedHashMap<String, Object>>() {
                });
        var content = objectMapper.convertValue(tripsMap.get("content"),
                new TypeReference<List<TripV2Dto>>() {
                });
        assertEquals(3, content.size());
        assertEquals(trip1.getRouteHumanReadableId(), content.get(0).routeHumanReadableId());
        assertEquals(trip2.getRouteHumanReadableId(), content.get(1).routeHumanReadableId());
        assertEquals(trip3.getRouteHumanReadableId(), content.get(2).routeHumanReadableId());

        var assertionList = List.of(trip1.getId(), trip2.getId(), trip3.getId());
        var forCheck = content.stream().filter(trip -> assertionList.contains(trip.id())).toList();
        assertEquals(3, forCheck.size());

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/")
                        .param("humanReadableId", "TP-0001-00000001")
                        .param("desireDateStart", OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(10).toString())
                        .param("desireDateEnd", OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(10).toString())
                        .with(buildDispatherJwt())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>() {
        });
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<>() {
        });
        assertEquals(1, content.size());
        assertEquals(trip1.getId(), content.get(0).id());
        assertEquals(trip1.getLoaders(), content.get(0).loaders());

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/")
                        .param("requestHumanReadableId", "OT-0033-00106240")
                        .with(buildDispatherJwt())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {
        });
        assertEquals(1, content.size());
        assertEquals(trip1.getId(), content.get(0).id());
    }

    @DisplayName("Проверка получения поездок по ID водителя")
    @Test
    void test_getAllByDriverIds() throws Exception {
        trip1 = dslContext.selectFrom(Tables.TRIPS).where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();
        trip1.setDriverId(driver.getId());
        dslContext.update(Tables.TRIPS).set(trip1).where(Tables.TRIPS.ID.eq(trip1.getId())).execute();
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/")
                        .param("driverIds", driver.getId().toString())
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {
        });
        Assertions.assertEquals(1, content.size());
        Assertions.assertEquals(trip1.getId(), content.get(0).id());
        Assertions.assertEquals(trip1.getDriverId(), content.get(0).driver().id());
    }

    @DisplayName("Проверка получения поездок по ID автопарка")
    @Test
    void test_getAllByAutoparkId() throws Exception {
        trip1 = dslContext.selectFrom(Tables.TRIPS).where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();
        trip1.setAutoparkId(autopark.getId());
        dslContext.update(Tables.TRIPS).set(trip1).where(Tables.TRIPS.ID.eq(trip1.getId())).execute();
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/")
                        .param("autoparkId", autopark.getId().toString())
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {
        });
        Assertions.assertEquals(1, content.size());
        Assertions.assertEquals(trip1.getId(), content.get(0).id());
    }

    @Test
    void testPatchTripByDispatcher() throws Exception {

        var dispatcherStartTime = OffsetDateTime.parse("2024-06-04T00:00:00+03:00",
                DateTimeFormatter.ISO_DATE_TIME).plusDays(1);
        String content1 = """
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
                        "field": "loadersWorkTime",
                        "value": "60000"
                    },
                    {
                        "field": "driverWaitingTime",
                        "value": "%s"
                    },
                    {
                        "field": "dispatcherStartTime",
                        "value": "%s"
                    },
                    {
                        "field": "loaders",
                        "value": "2"
                    }
                ]
                """.formatted(driver.getId(), Duration.ofMinutes(1).toMillis(), dispatcherStartTime);
        doPatchCurl(contractor, trip1, buildDispatherJwt(), content1);
        var actual = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();

        Assertions.assertNotNull(actual);
        assertEquals(12.0, actual.getFactDistance());
        assertEquals(60000, actual.getLoadersWorkTime());
        assertEquals(driver.getId(), actual.getDriverId());
        assertEquals(TripStatus.DRIVER_ASSIGNED.name(), actual.getStatus());
        assertEquals(Duration.ofMinutes(1).toMillis(), actual.getDriverWaitingTime());
        assertEquals(dispatcherStartTime, actual.getDispatcherStartTime());
        assertEquals(2, actual.getLoaders());

        doPatchCurl(contractor, trip1, buildDispatherJwt(), """
                [
                    {
                        "field": "status",
                        "value": "DRIVER_ON_THE_WAY"
                    }
                ]
                """);
        actual = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();
        var actualDriver = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();
        Assertions.assertNotNull(actual);
        assertEquals(TripStatus.DRIVER_ON_THE_WAY.name(), actual.getStatus());
        assertEquals(driver.getId(), actual.getDriverId());
        Assertions.assertNotNull(actualDriver);
        assertEquals(actual.getId(), actualDriver.getActiveTripId());
        assertEquals(false, actualDriver.getServing());

        doPatchCurl(contractor, trip1, buildDispatherJwt(), """
                [
                    {
                        "field": "status",
                        "value": "DRIVER_ARRIVED"
                    }
                ]
                """);
        actual = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();
        actualDriver = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();
        Assertions.assertNotNull(actual);
        assertEquals(TripStatus.DRIVER_ARRIVED.name(), actual.getStatus());
        assertEquals(driver.getId(), actual.getDriverId());
        Assertions.assertNotNull(actualDriver);
        assertEquals(actual.getId(), actualDriver.getActiveTripId());
        assertEquals(false, actualDriver.getServing());

        doPatchCurl(contractor, trip1, buildDispatherJwt(), """
                [
                    {
                        "field": "status",
                        "value": "TRIP_IN_PROGRESS"
                    }
                ]
                """);
        actual = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();
        actualDriver = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();
        Assertions.assertNotNull(actual);
        assertEquals(TripStatus.TRIP_IN_PROGRESS.name(), actual.getStatus());
        assertEquals(driver.getId(), actual.getDriverId());
        Assertions.assertNotNull(actualDriver);
        assertEquals(actual.getId(), actualDriver.getActiveTripId());
        assertEquals(true, actualDriver.getServing());

        doPatchCurl(contractor, trip1, buildDispatherJwt(), """
                [
                    {
                        "field": "status",
                        "value": "ORDER_FINISHED"
                    }
                ]
                """);
        actual = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();
        actualDriver = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();
        Assertions.assertNotNull(actual);
        assertEquals(TripStatus.ORDER_FINISHED.name(), actual.getStatus());
        assertEquals(driver.getId(), actual.getDriverId());
        Assertions.assertNotNull(actualDriver);
        Assertions.assertNull(actualDriver.getActiveTripId());
        assertEquals(false, actualDriver.getServing());

        doPatchCurl(contractor, trip2, buildDispatherJwt(), content1);
        actual = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(trip2.getId())).fetchOne();

        Assertions.assertNotNull(actual);
        assertEquals(12.0, actual.getFactDistance());
        assertEquals(60000, actual.getLoadersWorkTime());
        assertEquals(driver.getId(), actual.getDriverId());
        assertEquals(TripStatus.DRIVER_ASSIGNED.name(), actual.getStatus());

        doPatchCurl(contractor, trip2, buildDispatherJwt(), """
                [
                    {
                        "field": "status",
                        "value": "DRIVER_ON_THE_WAY"
                    }
                ]
                """);
        actual = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(trip2.getId())).fetchOne();
        actualDriver = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();
        Assertions.assertNotNull(actual);
        assertEquals(TripStatus.DRIVER_ON_THE_WAY.name(), actual.getStatus());
        assertEquals(driver.getId(), actual.getDriverId());
        Assertions.assertNotNull(actualDriver);
        assertEquals(actual.getId(), actualDriver.getActiveTripId());
        assertEquals(false, actualDriver.getServing());

        shift.setEndDate(LocalDateTime.now(ZoneOffset.UTC));
        dslContext.update(Tables.SHIFT).set(shift).execute();

        doPatchCurl(contractor, trip2, buildDispatherJwt(), """
                [
                    {
                        "field": "status",
                        "value": "ORDER_CANCELLED_BY_DRIVER"
                    }
                ]
                """);
        actual = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(trip2.getId())).fetchOne();
        actualDriver = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();

        Assertions.assertNotNull(actual);
        assertEquals(TripStatus.ORDER_CANCELLED_BY_DRIVER.name(), actual.getStatus());
        Assertions.assertNotNull(actualDriver);
        assertEquals(false, actualDriver.getOnline());
        Assertions.assertNull(actualDriver.getShiftId());

    }

    @Test
    @SneakyThrows
    void testShouldPatchFinishTimeDispatcher() {
        // given
        String patchStatus = """
                [
                    {
                       "field": "status",
                        "value": "ORDER_FINISHED"
                    }
                ]
                """;

        doPatchCurl(contractor, trip1, buildDispatherJwt(), patchStatus);

        OffsetDateTime finishTime = OffsetDateTime.now().withNano(0);
        String contet1 = """
                [
                    {
                        "field": "finishTime",
                        "value": "%s"
                    }
                ]
                """.formatted(finishTime);

        // when
        doPatchCurl(contractor, trip1, buildDispatherJwt(), contet1);

        // then
        var actual = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();

        assertThat(actual)
                .isNotNull()
                .extracting(TripsRecord::getFinishTime)
                .isEqualTo(finishTime);


        var trip = Instancio.of(Trip.class)
                .set(Select.field(Trip::getFinishTime), actual.getFinishTime()).create();

        tripDispatcherSender.send(trip, false, ChannelType.KAFKA);
        var captor = ArgumentCaptor.forClass(ContractorUpdateTripMessage.class);
        verify(outputBridge).send(captor.capture());
        var contractorUpdateTripMessage = captor.getValue();
        assertThat(contractorUpdateTripMessage.finishTime()).isNotNull();
        assertThat(contractorUpdateTripMessage.finishTime()).isEqualTo(finishTime.toInstant().atOffset(ZoneOffset.UTC));
    }

    @Test
    @SneakyThrows
    void shouldNotThrowWhenFinishTimeIsWrongType() {
        // given
        String patchStatus = """
                [
                    {
                       "field": "status",
                        "value": "ORDER_FINISHED"
                    }
                ]
                """;

        doPatchCurl(contractor, trip1, buildDispatherJwt(), patchStatus);

        String contet1 = """
                [
                    {
                        "field": "finishTime",
                        "value": "123"
                    }
                ]
                """;

        String url = "/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/";

        // when
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                        .with(buildDispatherJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contet1)
                )
                // then
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void testShouldPatchFactCostDispatcher() {
        // given
        String patchStatus = """
                [
                    {
                       "field": "status",
                        "value": "ORDER_FINISHED"
                    }
                ]
                """;

        doPatchCurl(contractor, trip1, buildDispatherJwt(), patchStatus);

        int factCost = 13;
        String contet1 = """
                [
                    {
                        "field": "factCost",
                        "value": "%s"
                    }
                ]
                """.formatted(factCost);

        // when
        doPatchCurl(contractor, trip1, buildDispatherJwt(), contet1);

        // then
        var actual = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();

        assertThat(actual)
                .isNotNull()
                .extracting(TripsRecord::getFactCost)
                .isEqualTo(factCost);

        var trip = Instancio.of(Trip.class)
                .set(Select.field(Trip::getFactCost), actual.getFactCost()).create();

        tripDispatcherSender.send(trip, false, ChannelType.KAFKA);
        var captor = ArgumentCaptor.forClass(ContractorUpdateTripMessage.class);
        verify(outputBridge).send(captor.capture());
        var contractorUpdateTripMessage = captor.getValue();
        assertThat(contractorUpdateTripMessage.factCost()).isNotNull();
        assertThat(contractorUpdateTripMessage.factCost()).isEqualTo(factCost);
    }

    @Test
    @SneakyThrows
    void testShouldNotSendFactCostWhenStatusIsNotORDER_FINISHED() {
        // given
        int factCost = 12;
        String contet1 = """
                [
                    {
                        "field": "factCost",
                        "value": "%s"
                    },
                    {
                        "field": "status",
                        "value": "ORDER_CANCELLED_BY_DRIVER"
                    }
                ]
                """.formatted(factCost);

        // when
        doPatchCurl(contractor, trip1, buildDispatherJwt(), contet1);

        // then
        var actual = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();

        assertThat(actual)
                .isNotNull()
                .extracting(TripsRecord::getFactCost)
                .isNull();

        var trip = Instancio.of(Trip.class)
                .set(Select.field(Trip::getFactCost), actual.getFactCost()).create();

        tripDispatcherSender.send(trip, false, ChannelType.KAFKA);
        var captor = ArgumentCaptor.forClass(ContractorUpdateTripMessage.class);
        verify(outputBridge).send(captor.capture());
        var contractorUpdateTripMessage = captor.getValue();
        assertThat(contractorUpdateTripMessage.factCost()).isNull();

    }


    @Test
    void testPatchDriverWaitingTimeTripByDispatcher() throws Exception {

        var driverWaitingTime = Duration.ofMinutes(1).toMillis();
        String contet1 = """
                [
                    {
                        "field": "driverWaitingTime",
                        "value": "%s"
                    }
                ]
                """.formatted(driverWaitingTime);
        doPatchCurl(contractor, trip1, buildDispatherJwt(), contet1);

        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/?humanReadableId=" + trip1.getRouteHumanReadableId())
                        .with(buildDispatherJwt())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {
        });
        assertEquals(1, content.size());
        assertEquals(driverWaitingTime, content.get(0).driverWaitingTime());
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 243, 3345, 0, 500})
    void testPatchDriverWaitingTimeTripByDispatcherCheckRange(long driverWaitingTimeMins) throws Exception {

        var driverWaitingTime = Duration.ofMinutes(driverWaitingTimeMins).toMillis();
        String content1 = """
                [
                    {
                        "field": "driverWaitingTime",
                        "value": "%s"
                    }
                ]
                """.formatted(driverWaitingTime);

        String url = "/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/";

        var patch = mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .with(buildDispatherJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content1)
        );

        if (driverWaitingTimeMins < 0 || driverWaitingTimeMins > 500) {
            patch.andExpect(MockMvcResultMatchers.status().isConflict());
            var duration = Duration.ofMinutes(driverWaitingTimeMins);
            Exception exception = Assertions.assertThrows(ConflictException.class, () -> verificationService.checkDriverWaitingTimeValueRangeCorrectness(duration));
            String actualMessage = exception.getMessage();
            assertThat(actualMessage).matches("Время ожидания водителя " + driverWaitingTimeMins + " мин. превышает допустимый диапазон от 0 до 500 мин.");
        } else {
            patch.andExpect(MockMvcResultMatchers.status().isOk());
            var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/?humanReadableId=" + trip1.getRouteHumanReadableId())
                            .with(buildDispatherJwt())
                    ).andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();
            var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
            });
            var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {
            });
            assertEquals(1, content.size());
            assertEquals(driverWaitingTime, content.get(0).driverWaitingTime());
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 243, 3345, 0, 500})
    void testPatchLoadersWorkTimeTripByDispatcherCheckRange(long loadersWorkTimeMins) throws Exception {

        String content1 = """
                [
                    {
                        "field": "loadersWorkTime",
                        "value": "%s"
                    }
                ]
                """.formatted(loadersWorkTimeMins * 60000);

        String url = "/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/";

        var patch = mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .with(buildDispatherJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content1)
        );

        if (loadersWorkTimeMins < 0 || loadersWorkTimeMins > 500) {
            patch.andExpect(MockMvcResultMatchers.status().isConflict());
            Exception exception = Assertions.assertThrows(ConflictException.class, () -> verificationService.checkLoadersWorkTimeValueRangeCorrectness(loadersWorkTimeMins * 60000));
            String actualMessage = exception.getMessage();
            assertThat(actualMessage).matches("Время работы грузчиков " + loadersWorkTimeMins + " мин. превышает допустимый диапазон от 0 до 500 мин.");
        } else {
            patch.andExpect(MockMvcResultMatchers.status().isOk());
            var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/?humanReadableId=" + trip1.getRouteHumanReadableId())
                            .with(buildDispatherJwt())
                    ).andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();
            var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
            });
            var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {
            });
            assertEquals(1, content.size());
            assertEquals(loadersWorkTimeMins * 60000, content.get(0).loadersWorkTime());
        }
    }

    @Test
    void testPatchTripByDriver() throws Exception {
        doPatchCurl(contractor, trip1, buildDispatherJwt(), """
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
                """.formatted(driver.getId()));

        doPatchCurl(contractor, trip1, buildDriverJwt(), """
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
                    },
                    {
                        "field": "qrs",
                        "value":
                        {
                            "waypointId": "5b1aaf8f-4c64-41fa-9efe-30058fab4eef",
                            "requestHumanReadableId": "OT-0025-00001281",
                            "qrs": [
                                "1234545", "1353555"
                            ]
                        }
                    }
                ]
                """);

        var actual = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();

        var actualCheckIns = Arrays.stream(dslContext.selectFrom(Tables.CHECK_IN)
                .where(Tables.CHECK_IN.TRIP_ID.eq(trip1.getId())).fetchArray()).toList();

        Assertions.assertNotNull(actual);
        assertEquals(driver.getId(), actual.getDriverId());
        assertEquals(TripStatus.DRIVER_ON_THE_WAY.name(), actual.getStatus());
        assertEquals(1, actualCheckIns.size());
        assertEquals(55.83938831333333, actualCheckIns.get(0).getLatitude());
        assertEquals(49.12604832333333, actualCheckIns.get(0).getLongitude());
        assertEquals("MANUAL", actualCheckIns.get(0).getType());
    }

    @Test
    void testGetAllByDispatcher() throws Exception {
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/dispatcher/" + dispatcher.getId().toString() + "/")
                        .with(buildDispatherJwt())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {
        });
        assertEquals(2, content.size());
        var assertionList = List.of(trip1.getId(), trip2.getId());
        var forCheck = content.stream().filter(trip -> assertionList.contains(trip.id())).collect(Collectors.toList());
        assertEquals(2, forCheck.size());
        assertEquals(dispatcher.getId(), forCheck.get(0).dispatcher().id());
        assertEquals(dispatcher.getId(), forCheck.get(1).dispatcher().id());
    }

    @Test
    void testGetTrip() throws Exception {
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId().toString() + "/")
                        .with(buildDispatherJwt())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var trip = objectMapper.readValue(response.getResponse().getContentAsString(), TripV2Dto.class);
        Assertions.assertNotNull(trip);
        assertEquals(trip1.getId(), trip.id());
    }

    @Test
    void testGetTripsByDriver() throws Exception {
        String content1 = """
                [
                    {
                        "field": "driverId",
                        "value": "%s"
                    }
                ]
                """;
        doPatchCurl(contractor, trip1, buildDispatherJwt(), content1.formatted(driver.getId()));

        doPatchCurl(contractor, trip2, buildDispatherJwt(), content1.formatted(driver.getId()));

        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/driver/" + driver.getId().toString() + "/")
                        .with(buildDriverJwt())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var tripsMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        var content = objectMapper.convertValue(tripsMap.get("content"), new TypeReference<List<TripV2Dto>>() {
        });
        assertEquals(2, content.size());
        var assertionList = List.of(trip1.getId(), trip2.getId());
        var forCheck = content.stream().filter(trip -> assertionList.contains(trip.id())).collect(Collectors.toList());
        assertEquals(2, forCheck.size());
        assertEquals(driver.getId(), forCheck.get(0).driver().id());
        assertEquals(driver.getId(), forCheck.get(1).driver().id());
    }

    @Test
    void testGetTripByDriver() throws Exception {
        doPatchCurl(contractor, trip1, buildDispatherJwt(), """
                [
                    {
                        "field": "driverId",
                        "value": "%s"
                    }
                ]
                """.formatted(driver.getId()));

        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/driver/" + driver.getId().toString() + "/trip/" + trip1.getId().toString() + "/")
                        .with(buildDriverJwt())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var trip = objectMapper.readValue(response.getResponse().getContentAsString(), TripV2Dto.class);
        Assertions.assertNotNull(trip);
        assertEquals(trip1.getId(), trip.id());
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor buildDriverJwt() {
        return jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor buildDispatherJwt() {
        return jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private void doPatchCurl(ContractorsRecord contractor, TripsRecord trip, SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor postProcessor, String content) throws Exception {

        String url = "/contractor/" + contractor.getId().toString() + "/trip/" + trip.getId() + "/";

        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .with(postProcessor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }


    @Test
    void testGetTripCheckins() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/")
                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
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

        var response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId().toString() + "/checkin-info/")
                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        var content = objectMapper.readValue(response.getResponse().getContentAsString(), CheckinResponseDtoV2.class);
        Assertions.assertNotNull(content);
        Assertions.assertTrue(content.getChekins().isEmpty());
        Assertions.assertNull(content.getTripDuration());
        Assertions.assertFalse(content.isComplete());

        var statuses = List.of(TripStatus.DRIVER_ON_THE_WAY, TripStatus.DRIVER_ARRIVED, TripStatus.TRIP_IN_PROGRESS, TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED, TripStatus.TRIP_IN_PROGRESS, TripStatus.ORDER_FINISHED);
        for (TripStatus status : statuses) {
            mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/")
                    .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
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

        response = mockMvc.perform(MockMvcRequestBuilders.get("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId().toString() + "/checkin-info/")
                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(driver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        content = objectMapper.readValue(response.getResponse().getContentAsString(), CheckinResponseDtoV2.class);
        Assertions.assertNotNull(content);
        Assertions.assertEquals(6, content.getChekins().size());
        Assertions.assertNotNull(content.getTripDuration());
        Assertions.assertTrue(content.isComplete());
    }

    @Test
    void testPatchTripByDispatcherForPlanning() throws Exception {
        driver.setShiftId(null);
        driver.setOnline(false);
        dslContext.update(Tables.DRIVER).set(driver).execute();

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/")
                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).claim("scope", "DISPATCHER").jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
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
        var actual = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();

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

        trip1 = dslContext.selectFrom(Tables.TRIPS).where(Tables.TRIPS.ID.eq(trip1.getId())).fetchOne();
        trip1.setStatus(TripStatus.TRIP_IN_PROGRESS.name());
        dslContext.update(Tables.TRIPS).set(trip1).execute();

        mockMvc.perform(MockMvcRequestBuilders.patch("/contractor/" + contractor.getId().toString() + "/trip/" + trip1.getId() + "/")
                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).claim("scope", "DISPATCHER").jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
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
}
