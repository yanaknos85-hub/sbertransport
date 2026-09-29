package ru.sber.transport.trips.cargo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import ru.sber.transport.trips.cargo.business.dto.TripAssignStatisticDto;
import ru.sber.transport.trips.cargo.business.model.CargoRequest;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.*;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера статистики")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
public class StatisticControllerImplTest extends KafkaTest {

    @Autowired
    private MockMvc mockMvc;

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
        trip1.setStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        trip1.setEndTime(trip1.getStartTime().plusHours(1));
        trip1.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip1.setContractorId(contractor.getId());
        trip1.setWaypoints(JSON.json("[]"));
        trip1.setRequests(JSON.json("[{\"id\":\"6f629b1e-a6c5-415b-8043-d7c62f5ccb5b\",\"humanReadableId\":\"OT-0033-00106240\",\"author\":{\"id\":\"cb5ede52-669f-40ff-8dc1-c232482be415\",\"lastName\":\"Рассохин\",\"firstName\":\"Евгений\",\"patronymic\":\"Викторович\",\"mobilePhone\":\"+79819519519\",\"email\":null,\"organization\":null},\"sender\":{\"id\":\"fbea0c83-9fd9-46af-a8e4-2c0db4f5b065\",\"lastName\":\"Ахмедова\",\"firstName\":\"Виолетта\",\"patronymic\":\"Сергеевна\",\"mobilePhone\":null,\"email\":null,\"organization\":\"Доп.офис №9040/00828\"},\"recipient\":{\"id\":\"cb5ede52-669f-40ff-8dc1-c232482be415\",\"lastName\":\"Рассохин\",\"firstName\":\"Евгений\",\"patronymic\":\"Викторович\",\"mobilePhone\":null,\"email\":null,\"organization\":\"Управление сопровождения территориально-зависимого производства \"},\"creationTime\":\"2023-01-27T07:28:48.000000884+00:00\",\"approvalDate\":\"2023-01-27T07:28:46.000000951+00:00\",\"desiredDate\":\"2023-12-26T00:00:00+00:00\",\"timeZone\":\"+3\",\"transportType\":\"DEDICATED\",\"approvedBy\":{\"id\":\"cb5ede52-669f-40ff-8dc1-c232482be415\",\"lastName\":\"Рассохин\",\"firstName\":\"Евгений\",\"patronymic\":\"Викторович\",\"mobilePhone\":\"+79819519519\",\"email\":null,\"organization\":null},\"tariffId\":\"5c3e800a-9833-4898-991a-1bcf3d1c588d\",\"status\":\"CARGO_APPROVED\",\"statusCode\":0,\"approvalState\":\"AWAITING_APPROVAL\",\"expected\":{\"cost\":4.6771896E7,\"deliveryTime\":0,\"distance\":1826.91,\"time\":96043.000000000},\"requestOptions\":[],\"active\":true,\"length\":3.0,\"width\":21.0,\"height\":29.5,\"volume\":1890.0,\"weight\":1.0,\"occupiedPlacesCount\":1,\"cargoData\":[{\"id\":\"6556a22b-a78d-40d9-a43e-55bc38b6a374\",\"position\":1,\"name\":\"Пакет\",\"type\":\"другое\",\"category\":\"другое\",\"length\":3.0,\"width\":21.0,\"height\":29.5,\"volume\":1890.0,\"weight\":1.0,\"occupiedPlacesCount\":1,\"fragile\":false,\"needPackage\":false,\"packageId\":null,\"packageCount\":0}],\"comment\":\"ТЕСТ Дополнительная информация\",\"finishedTime\":null,\"transferTime\":null,\"shipmentTime\":null,\"timeWorkStart\":null,\"sourceLoaders\":1,\"destinationLoaders\":1,\"timeWorkFinish\":null}]"));
        trip1.setDigitId(BigInteger.ONE);
        trip1.setFactDistance(10.02);
        trip1.setCapacity(1.0);
        trip1.setLoadersWorkTime(23000L);
        trip1.setDispatcherId(dispatcher.getId());
        trip1.setRouteHumanReadableId("CT-0001-00000001");

        trip2 = new TripsRecord();
        trip2.setId(UUID.randomUUID());
        trip2.setStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        trip2.setEndTime(trip2.getStartTime().plusHours(1));
        trip2.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip2.setContractorId(contractor.getId());
        trip2.setWaypoints(JSON.json("[]"));
        trip2.setRequests(JSON.json("[]"));
        trip2.setDigitId(BigInteger.TWO);
        trip2.setFactDistance(10.02);
        trip1.setCapacity(1.0);
        trip1.setLoadersWorkTime(23000L);
        trip2.setDispatcherId(dispatcher.getId());
        trip2.setRouteHumanReadableId("CT-0001-00000002");

        trip3 = new TripsRecord();
        trip3.setId(UUID.randomUUID());
        trip3.setStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        trip3.setEndTime(trip3.getStartTime().plusHours(1));
        trip3.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip3.setContractorId(contractor.getId());
        trip3.setWaypoints(JSON.json("[]"));
        trip3.setRequests(JSON.json("[]"));
        trip3.setDigitId(BigInteger.valueOf(3L));
        trip3.setFactDistance(10.02);
        trip3.setCapacity(1.0);
        trip3.setLoadersWorkTime(23000L);
        trip3.setRouteHumanReadableId("CT-0001-00000003");

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
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var dto = objectMapper.readValue(response.getResponse().getContentAsString(), TripAssignStatisticDto.class);

        assertEquals(10, dto.getTotalCount());
        assertEquals(5, dto.getAssignCount());
        assertEquals(5, dto.getNotAssignCount());
    }

    private void createTrip(int index, TripStatus status, UUID contractorId) throws JsonProcessingException {
        var trip = new TripsRecord();
        trip.setId(UUID.randomUUID());
        trip.setDigitId(BigInteger.valueOf(index));
        trip.setContractorId(contractorId);
        trip.setStatus(status.name());
        trip.setStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(index));
        trip.setRequests(JSON.json(objectMapper.writeValueAsString(Set.of(createRequest(UUID.randomUUID())))));
        trip.setWaypoints(JSON.json(objectMapper.writeValueAsString(List.of())));

        dslContext.insertInto(Tables.TRIPS).set(trip).execute();
    }

    private CargoRequest createRequest(UUID id) {
        var request = new CargoRequest();

        request.setTariffId(UUID.randomUUID());
        request.setId(id);

        return request;
    }
}
