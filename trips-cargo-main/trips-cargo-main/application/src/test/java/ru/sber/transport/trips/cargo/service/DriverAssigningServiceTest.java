package ru.sber.transport.trips.cargo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.web.service.DriverAssigningService;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.*;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка сервиса автоназначения")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips_cargo")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
public class DriverAssigningServiceTest extends KafkaTest {

    @Autowired
    private DriverAssigningService driverAssigningService;

    @Autowired
    private DSLContext dslContext;

    private ContractorsRecord contractor;

    private DispatcherRecord dispatcher;

    private DriverRecord driver;

    private TripsRecord trip1;

    private TripsRecord trip2;

    private TripsRecord trip3;

    private ObjectMapper objectMapper;

    @BeforeEach
    void createData() throws JsonProcessingException {
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

        var stringRequests = "[{\"id\":\"f22c3824-78ab-4bc9-86a8-018c36bdbdea\",\"humanReadableId\":\"OT-0001-00109009\",\"author\":{\"id\":\"30d6bac3-c1bc-4ce3-a6a4-e15f3d489914\",\"lastName\":\"Бландин\",\"firstName\":\"Александр\",\"patronymic\":\"Дмитриевич\",\"mobilePhone\":\"+78001307001\",\"email\":null,\"organization\":null},\"sender\":{\"id\":\"30d6bac3-c1bc-4ce3-a6a4-e15f3d489914\",\"lastName\":\"Бландин\",\"firstName\":\"Александр\",\"patronymic\":\"Дмитриевич\",\"mobilePhone\":\"+78001307001\",\"email\":\"ADmBlandin@sberbank.ru\",\"organization\":\"ООО Отправка\"},\"recipient\":{\"id\":\"d52dc587-e1ab-4f3a-9ffa-9666e5245b6a\",\"lastName\":\"Медведев\",\"firstName\":\"Анатолий\",\"patronymic\":\"Дмитриевич\",\"mobilePhone\":\"+79956234832\",\"email\":\"MEDVEDEV.A.DMIT@OMEGA.SBRF.RU\",\"organization\":\"ООО Приемка\"},\"creationTime\":\"2023-10-25T06:09:06.00000011+00:00\",\"approvalDate\":\"2023-10-25T06:13:50.000000648+00:00\",\"desiredDate\":\"2023-10-26T12:00:00.000000658+00:00\",\"timeZone\":\"+3\",\"transportType\":\"DEDICATED\",\"approvedBy\":{\"id\":\"30d6bac3-c1bc-4ce3-a6a4-e15f3d489914\",\"lastName\":\"Бландин\",\"firstName\":\"Александр\",\"patronymic\":\"Дмитриевич\",\"mobilePhone\":\"+78001307001\",\"email\":null,\"organization\":null},\"tariffId\":\"782024b3-ee94-4cbd-a818-9ed4254a88ec\",\"status\":\"CARGO_APPROVED\",\"statusCode\":0,\"approvalState\":\"APPROVED\",\"expected\":{\"cost\":32822.0,\"deliveryTime\":2,\"distance\":53.261,\"time\":4251.0},\"requestOptions\":[],\"active\":true,\"length\":50,\"width\":25,\"height\":45,\"volume\":11750,\"weight\":5,\"occupiedPlacesCount\":2,\"cargoData\":[{\"id\":\"465f1bd3-40b3-44ef-a24e-4d11a64485ca\",\"position\":1,\"name\":\"Стул\",\"type\":\"Продовольственные товары\",\"category\":\"другое\",\"length\":20.0,\"width\":20.0,\"height\":20.0,\"volume\":8000.0,\"weight\":2.0,\"occupiedPlacesCount\":1,\"fragile\":false,\"needPackage\":false,\"packageId\":null,\"packageCount\":1},{\"id\":\"4c119618-e761-4028-8279-b0b46869c289\",\"position\":2,\"name\":\"Стеллаж-полка угловой коробка 2\",\"type\":\"другое\",\"category\":\"другое\",\"length\":30.0,\"width\":5.0,\"height\":25.0,\"volume\":3750.0,\"weight\":3.0,\"occupiedPlacesCount\":1,\"fragile\":false,\"needPackage\":false,\"packageId\":null,\"packageCount\":1}],\"comment\":\"\",\"finishedTime\":null,\"transferTime\":null,\"shipmentTime\":null,\"timeWorkStart\":null,\"sourceLoaders\":0,\"destinationLoaders\":0,\"timeWorkFinish\":null}]";
        var stringWaypoints = "[{\"id\":\"a4b63256-146b-45ad-a9fc-496318a5dd89\",\"latitude\":55.6120850000000004,\"longitude\":37.2006659999999982,\"orderingIndex\":1,\"country\":\"Россия\",\"region\":\"Москва\",\"city\":\"Москва\",\"street\":\"улица Ленина\",\"house\":\"5\",\"building\":null,\"waitingTime\":20},{\"id\":\"11401c3b-e37e-4261-9f01-55da3316d7e5\",\"latitude\":55.779477,\"longitude\":37.6444290000000024,\"orderingIndex\":2,\"country\":\"Россия\",\"region\":\"Москва\",\"city\":\"Москва\",\"street\":\"Протопоповский переулок\",\"house\":\"40\",\"building\":null,\"waitingTime\":20}]";

        trip1 = new TripsRecord();
        trip1.setId(UUID.randomUUID());
        trip1.setStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC).plusMinutes(28));
        trip1.setEndTime(trip1.getStartTime().plusHours(1));
        trip1.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip1.setContractorId(contractor.getId());
        trip1.setWaypoints(JSON.json(stringWaypoints));
        trip1.setRequests(JSON.json(stringRequests));
        trip1.setDigitId(BigInteger.ONE);
        trip1.setFactDistance(10.02);
        trip1.setCapacity(2.0);
        trip1.setLoadersWorkTime(23000L);
        trip1.setDispatcherId(dispatcher.getId());

        trip2 = new TripsRecord();
        trip2.setId(UUID.randomUUID());
        trip2.setStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        trip2.setEndTime(trip2.getStartTime().plusHours(1));
        trip2.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip2.setContractorId(contractor.getId());
        trip2.setWaypoints(JSON.json(stringWaypoints));
        trip2.setRequests(JSON.json(stringRequests));
        trip2.setDigitId(BigInteger.TWO);
        trip2.setFactDistance(10.02);
        trip2.setCapacity(2.0);
        trip2.setLoadersWorkTime(23000L);
        trip2.setDispatcherId(dispatcher.getId());

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
        trip3.setCapacity(2.0);
        trip3.setLoadersWorkTime(23000L);

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
        driver.setLatitude(55.6120850000000004);
        driver.setLongitude(37.2006659999999982);
        driver.setPointTime(OffsetDateTime.now());
        driver.setTimeZone("GMT+03");
        dslContext.update(Tables.DRIVER).set(driver).execute();

    }

    @Test
    void assignDriverTest(){
        driverAssigningService.assignDrivers();
        var assignedDriver = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(driver.getId())).fetchOne();
        Assertions.assertNotNull(assignedDriver);
        Assertions.assertNotNull(assignedDriver.getActiveTripId());
        Assertions.assertEquals(assignedDriver.getActiveTripId(), trip1.getId());
    }

}
