package ru.sber.transport.trips.cargo.file_resolvers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.spreadsheet.annotation.NullRender;
import ru.sber.transport.spreadsheet.annotation.TimeFormat;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.trips.cargo.business.dto.CheckinType;
import ru.sber.transport.trips.cargo.business.dto.Prefix;
import ru.sber.transport.trips.cargo.business.dto.TripsExportDto;
import ru.sber.transport.trips.cargo.business.model.*;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.messaging.providers.DispatcherProvider;
import ru.sber.transport.trips.cargo.messaging.providers.DriverProvider;
import ru.sber.transport.trips.cargo.messaging.providers.VehicleProvider;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.*;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"java:S5961", "SpringJavaInjectionPointsAutowiringInspection"})
@SpringBootTest
@Slf4j
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@DisplayName("Проверка импортёра файлов поездок")
@Transactional
@MockitoBean(types = Key.class)
@UnitTest
@IsolatedTest
@Feature("app_platform_trips_cargo")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
public class TripsResolverTest extends KafkaTest{

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContractorProvider contractorProvider;

    @Autowired
    private VehicleProvider vehicleProvider;

    @Autowired
    private DispatcherProvider dispatcherProvider;

    @Autowired
    private DriverProvider driverProvider;

    @Autowired
    private TripProvider tripProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthorizationManager<?> manager;

    @Autowired
    private DSLContext dslContext;

    private DispatcherRecord dispatcher;

    private TripsRecord trip1;
    private TripsRecord tripWithoutRequests;

    private final List<CheckInRecord> checkins = new ArrayList<>();

    private static ZoneOffset offsetZone = ZonedDateTime.now(ZoneId.systemDefault()).getOffset();


    @AfterEach
    void cleanUp() {
        dslContext.deleteFrom(Tables.SHIFT).execute();
        dslContext.deleteFrom(Tables.VEHICLE).execute();
        dslContext.deleteFrom(Tables.DRIVER).execute();
        dslContext.deleteFrom(Tables.TRIPS).execute();
        dslContext.deleteFrom(Tables.DISPATCHER).execute();
        dslContext.deleteFrom(Tables.CONTRACTORS).execute();
    }

    @SneakyThrows
    @Test
    @DisplayName("Проверка экспорта при наличии requests")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void exportData() {
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

        var requests = "[{\"id\":\"f22c3824-78ab-4bc9-86a8-018c36bdbdea\",\"humanReadableId\":\"OT-0001-00109009\",\"author\":{\"id\":\"30d6bac3-c1bc-4ce3-a6a4-e15f3d489914\",\"lastName\":\"Бландин\",\"firstName\":\"Александр\",\"patronymic\":\"Дмитриевич\",\"mobilePhone\":\"+78001307001\",\"email\":null,\"organization\":null},\"sender\":{\"id\":\"30d6bac3-c1bc-4ce3-a6a4-e15f3d489914\",\"lastName\":\"Бландин\",\"firstName\":\"Александр\",\"patronymic\":\"Дмитриевич\",\"mobilePhone\":\"+78001307001\",\"email\":\"ADmBlandin@sberbank.ru\",\"organization\":\"ООО Отправка\"},\"recipient\":{\"id\":\"d52dc587-e1ab-4f3a-9ffa-9666e5245b6a\",\"lastName\":\"Медведев\",\"firstName\":\"Анатолий\",\"patronymic\":\"Дмитриевич\",\"mobilePhone\":\"+79956234832\",\"email\":\"MEDVEDEV.A.DMIT@OMEGA.SBRF.RU\",\"organization\":\"ООО Приемка\"},\"creationTime\":\"2023-10-25T06:09:06.00000011+00:00\",\"approvalDate\":\"2023-10-25T06:13:50.000000648+00:00\",\"desiredDate\":\"2023-10-26T12:00:00.000000658+00:00\",\"timeZone\":\"+3\",\"transportType\":\"DEDICATED\",\"approvedBy\":{\"id\":\"30d6bac3-c1bc-4ce3-a6a4-e15f3d489914\",\"lastName\":\"Бландин\",\"firstName\":\"Александр\",\"patronymic\":\"Дмитриевич\",\"mobilePhone\":\"+78001307001\",\"email\":null,\"organization\":null},\"tariffId\":\"782024b3-ee94-4cbd-a818-9ed4254a88ec\",\"status\":\"CARGO_APPROVED\",\"statusCode\":0,\"approvalState\":\"APPROVED\",\"expected\":{\"cost\":32822.0,\"deliveryTime\":2,\"distance\":53.261,\"time\":4251.0},\"requestOptions\":[],\"active\":true,\"length\":50,\"width\":25,\"height\":45,\"volume\":11750,\"weight\":5,\"occupiedPlacesCount\":2,\"cargoData\":[{\"id\":\"465f1bd3-40b3-44ef-a24e-4d11a64485ca\",\"position\":1,\"name\":\"Стул\",\"type\":\"Продовольственные товары\",\"category\":\"другое\",\"length\":20.0,\"width\":20.0,\"height\":20.0,\"volume\":8000.0,\"weight\":2.0,\"occupiedPlacesCount\":1,\"fragile\":false,\"needPackage\":false,\"packageId\":null,\"packageCount\":1},{\"id\":\"4c119618-e761-4028-8279-b0b46869c289\",\"position\":2,\"name\":\"Стеллаж-полка угловой коробка 2\",\"type\":\"другое\",\"category\":\"другое\",\"length\":30.0,\"width\":5.0,\"height\":25.0,\"volume\":3750.0,\"weight\":3.0,\"occupiedPlacesCount\":1,\"fragile\":false,\"needPackage\":false,\"packageId\":null,\"packageCount\":1}],\"comment\":\"\",\"finishedTime\":null,\"transferTime\":null,\"shipmentTime\":null,\"timeWorkStart\":null,\"sourceLoaders\":0,\"destinationLoaders\":0,\"timeWorkFinish\":null}]";
        var waypoints = "[{\"id\":\"a4b63256-146b-45ad-a9fc-496318a5dd89\",\"latitude\":55.6120850000000004,\"longitude\":37.2006659999999982,\"orderingIndex\":1,\"country\":\"Россия\",\"region\":\"Москва\",\"city\":\"Москва\",\"street\":\"улица Ленина\",\"house\":\"5\",\"building\":null,\"waitingTime\":20},{\"id\":\"11401c3b-e37e-4261-9f01-55da3316d7e5\",\"latitude\":55.779477,\"longitude\":37.6444290000000024,\"orderingIndex\":2,\"country\":\"Россия\",\"region\":\"Москва\",\"city\":\"Москва\",\"street\":\"Протопоповский переулок\",\"house\":\"40\",\"building\":null,\"waitingTime\":20}]";

        trip1 = new TripsRecord();
        trip1.setCreationTime(OffsetDateTime.now(offsetZone));
        trip1.setId(UUID.randomUUID());
        trip1.setStartTime(OffsetDateTime.of(LocalDateTime.now(offsetZone), offsetZone));
        trip1.setEndTime(trip1.getStartTime().plusHours(1));
        trip1.setStatus(TripStatus.ORDER_FINISHED.name());
        trip1.setContractorId(contractor.getId());
        trip1.setWaypoints(JSON.json(waypoints));
        trip1.setRequests(JSON.json(requests));
        trip1.setDigitId(BigInteger.ONE);
        trip1.setFactDistance(10.02);
        trip1.setCapacity(1.0);
        trip1.setLoadersWorkTime(23000L);
        trip1.setDispatcherId(dispatcher.getId());
        trip1.setRouteHumanReadableId("CT-0001-0001");
        trip1.setDriverWaitingTime(Duration.ofMinutes(3600).toMillis());

        dslContext.insertInto(Tables.TRIPS).set(trip1).execute();

        var trip2 = new TripsRecord();
        trip2.setCreationTime(OffsetDateTime.now(offsetZone));
        trip2.setId(UUID.randomUUID());
        trip2.setStartTime(OffsetDateTime.of(LocalDateTime.now(offsetZone), offsetZone));
        trip2.setEndTime(trip1.getStartTime().plusHours(1));
        trip2.setStatus(TripStatus.ORDER_CANCELLED_BY_CLIENT.name());
        trip2.setContractorId(contractor.getId());
        trip2.setWaypoints(JSON.json(waypoints));
        trip2.setRequests(JSON.json(requests));
        trip2.setDigitId(BigInteger.TWO);
        trip2.setFactDistance(10.02);
        trip2.setCapacity(1.0);
        trip2.setLoadersWorkTime(23000L);
        trip2.setDispatcherId(dispatcher.getId());

        dslContext.insertInto(Tables.TRIPS).set(trip2).execute();

        var trip3 = new TripsRecord();
        trip3.setCreationTime(OffsetDateTime.now(offsetZone));
        trip3.setId(UUID.randomUUID());
        trip3.setStartTime(OffsetDateTime.of(LocalDateTime.now(offsetZone), offsetZone));
        trip3.setEndTime(trip1.getStartTime().plusHours(1));
        trip3.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        trip3.setContractorId(contractor.getId());
        trip3.setWaypoints(JSON.json(waypoints));
        trip3.setRequests(JSON.json("[]"));
        trip3.setDigitId(BigInteger.valueOf(3));
        trip3.setFactDistance(10.02);
        trip3.setCapacity(1.0);
        trip3.setLoadersWorkTime(23000L);
        trip3.setDispatcherId(dispatcher.getId());

        dslContext.insertInto(Tables.TRIPS).set(trip3).execute();

        var driver = new DriverRecord();
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
        shift.setStartDate(LocalDateTime.now(offsetZone).minusDays(1));
        shift.setEndDate(LocalDateTime.now(offsetZone).plusDays(1));
        shift.setActive(true);
        shift.setDeleted(false);

        dslContext.insertInto(Tables.SHIFT).set(shift).execute();

        driver.setShiftId(shift.getId());
        driver.setOnline(true);
        dslContext.update(Tables.DRIVER).set(driver).execute();

        trip1 = dslContext.selectFrom(Tables.TRIPS).where(Tables.TRIPS.ID.eq(trip1.getId())).fetchAny();
        trip1.setDriverId(driver.getId());
        trip1.setVehicleId(vehicle.getId());
        trip1.setLoaders(5);
        dslContext.update(Tables.TRIPS).set(trip1).execute();

        var checkin1 = new CheckInRecord();
        checkin1.setId(UUID.randomUUID());
        checkin1.setStatus(TripStatus.DRIVER_ARRIVED.name());
        checkin1.setTripId(trip1.getId());
        checkin1.setLatitude(55.6120850000000004);
        checkin1.setLongitude(37.2006659999999982);
        checkin1.setTime(OffsetDateTime.now(offsetZone));
        checkin1.setTimeZone(offsetZone.getId());
        checkin1.setType("AUTO");
        checkins.add(checkin1);

        var checkin2 = new CheckInRecord();
        checkin2.setId(UUID.randomUUID());
        checkin2.setStatus(TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED.name());
        checkin2.setTripId(trip1.getId());
        checkin2.setLatitude(55.6120850000000004);
        checkin2.setLongitude(37.2006659999999982);
        checkin2.setTime(OffsetDateTime.now(offsetZone));
        checkin2.setTimeZone(offsetZone.getId());
        checkin2.setType("MANUAL");
        checkins.add(checkin2);

        var checkin3 = new CheckInRecord();
        checkin3.setId(UUID.randomUUID());
        checkin3.setStatus(TripStatus.ORDER_FINISHED.name());
        checkin3.setTripId(trip1.getId());
        checkin3.setLatitude(55.6120850000000004);
        checkin3.setLongitude(37.2006659999999982);
        checkin3.setTime(OffsetDateTime.now(offsetZone));
        checkin3.setTimeZone(offsetZone.getId());
        checkin3.setType("AUTO");
        checkins.add(checkin3);

        checkins.forEach(checkInRecord -> dslContext.insertInto(Tables.CHECK_IN).set(checkInRecord).execute());

        var filters = """
                {
                    "duration" : {
                        "start":"%s",
                        "end":"%s"
                    },
                    "statuses" : [
                        "ORDER_FINISHED",
                        "WAITING_FOR_ASSIGNMENT",
                        "ORDER_CANCELLED_BY_CLIENT"
                    ]
                }
                """.formatted(OffsetDateTime.now(offsetZone).minusDays(2)
                ,OffsetDateTime.now(offsetZone).plusDays(2));
        var content = mockMvc.perform(get("/files/trips/?filters="+Base64.getEncoder().encodeToString(filters.getBytes(StandardCharsets.UTF_8)))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        var response = new ObjectMapper().readValue(content, new TypeReference<Map<String, String>>() {
        });

        var result = await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(30))
                .until(() -> mockMvc.perform(get(response.get("url") + "/")
                                .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse(), r -> {
                    try {
                        return !r.getContentAsString().contains("in_progress\": true");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                });

        try (var byteArrayInputStream = new ByteArrayInputStream(result.getContentAsByteArray());
             var excel = new XSSFWorkbook(byteArrayInputStream)) {
            var sheet = excel.getSheet("Поездки");

            assertThat(sheet).isNotNull();

            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(3); // 1, 2 - заголовок

            var cellIndex = 0;
            var row = sheet.getRow(0);
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("№");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("ID поездки");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("ID маршрута");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("ID заявки(-ок)");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Начало поездки");
            cellIndex++;// Дата
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Маршрут");
            cellIndex++;// Промеж
            cellIndex++;// Окончание
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Прохождение точек маршрута");
            cellIndex++;// Статусы
            cellIndex++;// Потверждение геопозиции
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Статус");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Комментарии для водителя");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Заказчик");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Номер ТС");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Водитель");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Время создания");
            cellIndex++;//Дата
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Количество грузчиков");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("План");
            cellIndex += 2;
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Факт");
            cellIndex += 3;
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Тип поездки"); //совмест-индив
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Номер договора");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Тариф");
            cellIndex++;
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("МВЗ");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Данные диспетчера");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Диспетчер");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Телефон диспетчера");

            row = sheet.getRow(1);
            cellIndex = 4;
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Дата");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Время");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Подача");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Промежуточные точки");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Окончание");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Количество точек маршрута");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Статус");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Геопозиция подтверждена");
            cellIndex +=5;
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Дата");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Время");
            cellIndex++;
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Протяженность (км)");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Ожидание (мин)");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Стоимость (руб)");

            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Протяженность (км)");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Стоимость (руб)");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Время поездки (мин)");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Время ожидания (мин)");
            cellIndex += 2;
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Время ожидания (руб/мин)");
            assertThat(row.getCell(cellIndex).getStringCellValue()).isEqualTo("Стоимость (руб/мин)");

            var actualRow = sheet.getRow(2);

            var tripWaypoints = objectMapper.readValue(trip1.getWaypoints().data(), new TypeReference<ArrayList<Waypoint>>() {});

            var requestsTrip = objectMapper.readValue(trip1.getRequests().data(), new TypeReference<ArrayList<CargoRequest>>() {});
            var authors = requestsTrip.stream().map(CargoRequest::getAuthor).toList();
            var waypointList = new LinkedList<>(tripWaypoints.stream().sorted(Comparator.comparing(Waypoint::orderingIndex)).toList());
            var driverId = trip1.getDriverId();
            var checkinData = getCheckinData(checkins, waypointList, trip1.getId());
            var commonWaitTime = waypointList.stream().map(Waypoint::waitingTime).reduce(Duration.ZERO, Duration::plus);
            var tripDuration = getTripFactDuration(checkins);

            cellIndex = 0;
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(1);
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("%s-%04d-%08d".formatted(Prefix.TC, contractorProvider.getContractorDigitId(trip1.getContractorId()), trip1.getDigitId()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(trip1.getRouteHumanReadableId());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(requestsTrip.stream().map(CargoRequest::getHumanReadableId).collect(Collectors.joining("; ")));
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(DateUtil.getExcelDate(trip1.getStartTime().toLocalDate()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(trip1.getStartTime().withOffsetSameInstant(offsetZone).toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString());
//            cellIndex++;
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(getAddressFromWaypoint(waypointList.getFirst()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(waypointList.stream().skip(1).limit(waypointList.size() - 2).map(this::getAddressFromWaypoint).collect(Collectors.joining("; ")));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(getAddressFromWaypoint(waypointList.getLast()));
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(checkinData.quantity().intValue());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(checkinData.status());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(checkinData.waypointConfirmation());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapStatus(TripStatus.valueOf(trip1.getStatus())));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(requestsTrip.stream().map(CargoRequest::getComment).collect(Collectors.joining("; ")));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(authors.stream().map(this::mapName).collect(Collectors.joining("; ")));
            cellIndex++;
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapFullName(driverProvider.get(driverId).get()));
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(DateUtil.getExcelDate(trip1.getCreationTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of(offsetZone.getId()).normalized().getId())).toLocalDate()));
//            cellIndex++;
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(trip1.getCreationTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of(offsetZone.getId()).normalized().getId())).toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString());
//            cellIndex++;
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(trip1.getLoaders().intValue());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("%s:%02d".formatted(commonWaitTime.toMinutes(), commonWaitTime.toSecondsPart()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(trip1.getFactDistance());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("%s:%02d".formatted(tripDuration.toMinutes(), tripDuration.toSecondsPart()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Грузовая");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).hasToString("3600:00");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapName(dispatcherProvider.get(trip1.getDispatcherId()).get()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(dispatcherProvider.get(trip1.getDispatcherId()).get().getPhone());
        }
    }

    @SneakyThrows
    @Test
    @DisplayName("Экспорт данных при отсутствии requests")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void exportDataFromWaypointsTest() {
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

        var requstsEmpty = "[]";
        var waypointsWithHumanReadableId = """
                [{"id":"5b1aaf8f-4c64-41fa-9efe-30058fab4eef","latitude":55.787662,"longitude":37.509508,"orderingIndex":1,"country":null,"region":null,"city":null,"street":null,"house":null,"building":null,"waitingTime":null,"address":"г.Краснодар, ул.Селезнева, 136","contacts":[{"contact":{"fullName":null,"phone":"+79998887766"},"requests":[{"humanReadableId":"OT-0025-00001281","type":"LOAD","organization":"ООО Ромашка","cargo":[{"orderingIndex":0,"cargoName":"Коробка","weight":7.5,"volume":90000.0,"occupiedPlacesCount":2,"height":30.0,"length":50.0,"width":60.0,"fragile":true}],"loaders":1,"comment":"Осторожно, не бросать !","pack":[{"name":"Гофрокороб пятислойный (610мм*400мм*330мм)","unit":"штука","count":2}]}]}]},{"id":"5b1aaf8f-4c64-41fa-9efe-30058fab4eef","latitude":55.787662,"longitude":37.509508,"orderingIndex":1,"country":null,"region":null,"city":null,"street":null,"house":null,"building":null,"waitingTime":null,"address":"г.Краснодар, ул.Селезнева, 136","contacts":[{"contact":{"fullName":null,"phone":"+79998887766"},"requests":[{"humanReadableId":"OT-0025-00001281","type":"LOAD","organization":"ООО Ромашка","cargo":[{"orderingIndex":0,"cargoName":"Коробка","weight":7.5,"volume":90000.0,"occupiedPlacesCount":2,"height":30.0,"length":50.0,"width":60.0,"fragile":true}],"loaders":1,"comment":"Осторожно, не бросать !","pack":[{"name":"Гофрокороб пятислойный (610мм*400мм*330мм)","unit":"штука","count":2}]}]}]}]
                """;

        var driver = new DriverRecord();
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

        tripWithoutRequests = new TripsRecord();
        tripWithoutRequests.setCreationTime(OffsetDateTime.now(offsetZone));
        tripWithoutRequests.setId(UUID.randomUUID());
        tripWithoutRequests.setStartTime(OffsetDateTime.of(LocalDateTime.now(offsetZone), offsetZone));
        tripWithoutRequests.setEndTime(tripWithoutRequests.getStartTime().plusHours(1));
        tripWithoutRequests.setStatus(TripStatus.ORDER_FINISHED.name());
        tripWithoutRequests.setContractorId(contractor.getId());
        tripWithoutRequests.setWaypoints(JSON.json(waypointsWithHumanReadableId));
        tripWithoutRequests.setRequests(JSON.json(requstsEmpty));
        tripWithoutRequests.setDigitId(BigInteger.valueOf(4L));
        tripWithoutRequests.setFactDistance(10.02);
        tripWithoutRequests.setCapacity(1.0);
        tripWithoutRequests.setLoadersWorkTime(23000L);
        tripWithoutRequests.setDispatcherId(dispatcher.getId());
        tripWithoutRequests.setRouteHumanReadableId("CT-0001-0004");
        tripWithoutRequests.setDriverWaitingTime(Duration.ofMinutes(3600).toMillis());
        tripWithoutRequests.setTimeZone(offsetZone.getId());

        dslContext.insertInto(Tables.TRIPS).set(tripWithoutRequests).execute();

        var driverForTripWithoutRequests = new DriverRecord();
        driverForTripWithoutRequests.setId(UUID.randomUUID());
        driverForTripWithoutRequests.setHumanReadableId("DR-0001-0004");
        driverForTripWithoutRequests.setLastName("LastName");
        driverForTripWithoutRequests.setFirstName("FirstName");
        driverForTripWithoutRequests.setPatronymic("Patronymic");
        driverForTripWithoutRequests.setContractorId(contractor.getId());
        driverForTripWithoutRequests.setActive(true);
        driverForTripWithoutRequests.setRating(2);
        driverForTripWithoutRequests.setServing(false);
        driverForTripWithoutRequests.setOnline(false);
        driverForTripWithoutRequests.setContactPhone("+79399912275");
        driverForTripWithoutRequests.setEmail("mail@mail.ru");

        dslContext.insertInto(Tables.DRIVER).set(driverForTripWithoutRequests).execute();

        var vehicleForTripWithoutRequests = new VehicleRecord();
        vehicleForTripWithoutRequests.setId(UUID.randomUUID());
        vehicleForTripWithoutRequests.setBrand("Brand");
        vehicleForTripWithoutRequests.setModel("Model");
        vehicleForTripWithoutRequests.setColor("Color");
        vehicleForTripWithoutRequests.setContractorId(contractor.getId());
        vehicleForTripWithoutRequests.setDeleted(false);
        vehicleForTripWithoutRequests.setStateNumber("A996AA178RUS");

        dslContext.insertInto(Tables.VEHICLE).set(vehicleForTripWithoutRequests).execute();

        var shiftForTripWithoutRequests = new ShiftRecord();
        shiftForTripWithoutRequests.setId(UUID.randomUUID());
        shiftForTripWithoutRequests.setContractorId(contractor.getId());
        shiftForTripWithoutRequests.setDriverId(driver.getId());
        shiftForTripWithoutRequests.setVehicleId(vehicle.getId());
        shiftForTripWithoutRequests.setStartDate(LocalDateTime.now(offsetZone).minusDays(1));
        shiftForTripWithoutRequests.setEndDate(LocalDateTime.now(offsetZone).plusDays(1));
        shiftForTripWithoutRequests.setActive(true);
        shiftForTripWithoutRequests.setDeleted(false);

        dslContext.insertInto(Tables.SHIFT).set(shiftForTripWithoutRequests).execute();

        driverForTripWithoutRequests.setShiftId(shiftForTripWithoutRequests.getId());
        driverForTripWithoutRequests.setOnline(true);
        dslContext.update(Tables.DRIVER)
                .set(Tables.DRIVER.SHIFT_ID, driverForTripWithoutRequests.getShiftId())
                .set(Tables.DRIVER.ONLINE, driverForTripWithoutRequests.getOnline())
                .where(Tables.DRIVER.ID.eq(driverForTripWithoutRequests.getId()))
                .execute();

        tripWithoutRequests = dslContext.selectFrom(Tables.TRIPS).where(Tables.TRIPS.ID.eq(tripWithoutRequests.getId())).fetchAny();
        tripWithoutRequests.setDriverId(driver.getId());
        tripWithoutRequests.setVehicleId(vehicle.getId());
        tripWithoutRequests.setLoaders(5);
        dslContext.update(Tables.TRIPS).set(tripWithoutRequests).execute();

        var checkin1ForTripWithoutRequests = new CheckInRecord();
        checkin1ForTripWithoutRequests.setId(UUID.randomUUID());
        checkin1ForTripWithoutRequests.setStatus(TripStatus.DRIVER_ARRIVED.name());
        checkin1ForTripWithoutRequests.setTripId(tripWithoutRequests.getId());
        checkin1ForTripWithoutRequests.setLatitude(55.6120850000000004);
        checkin1ForTripWithoutRequests.setLongitude(37.2006659999999982);
        checkin1ForTripWithoutRequests.setTime(OffsetDateTime.now(offsetZone));
        checkin1ForTripWithoutRequests.setTimeZone(offsetZone.getId());
        checkin1ForTripWithoutRequests.setType("AUTO");
        checkins.add(checkin1ForTripWithoutRequests);

        var checkin3ForTripWithoutRequests = new CheckInRecord();
        checkin3ForTripWithoutRequests.setId(UUID.randomUUID());
        checkin3ForTripWithoutRequests.setStatus(TripStatus.ORDER_FINISHED.name());
        checkin3ForTripWithoutRequests.setTripId(tripWithoutRequests.getId());
        checkin3ForTripWithoutRequests.setLatitude(55.6120850000000004);
        checkin3ForTripWithoutRequests.setLongitude(37.2006659999999982);
        checkin3ForTripWithoutRequests.setTime(OffsetDateTime.now(offsetZone));
        checkin3ForTripWithoutRequests.setTimeZone(offsetZone.getId());
        checkin3ForTripWithoutRequests.setType("AUTO");
        checkins.add(checkin3ForTripWithoutRequests);

        checkins.forEach(checkInRecord -> dslContext.insertInto(Tables.CHECK_IN).set(checkInRecord).execute());

        var filters = """
                {
                    "duration" : {
                        "start":"%s",
                        "end":"%s"
                    },
                    "statuses" : [
                        "ORDER_FINISHED",
                        "WAITING_FOR_ASSIGNMENT",
                        "ORDER_CANCELLED_BY_CLIENT"
                    ]
                }
                """.formatted(OffsetDateTime.now(offsetZone).minusDays(2)
                ,OffsetDateTime.now(offsetZone).plusDays(2));
        var content = mockMvc.perform(get("/files/trips/?filters="+Base64.getEncoder().encodeToString(filters.getBytes(StandardCharsets.UTF_8)))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        var response = new ObjectMapper().readValue(content, new TypeReference<Map<String, String>>() {
        });

        var result = await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(30))
                .until(() -> mockMvc.perform(get(response.get("url") + "/")
                                .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse(), r -> {
                    try {
                        return !r.getContentAsString().contains("in_progress\": true");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                });

        try (var byteArrayInputStream = new ByteArrayInputStream(result.getContentAsByteArray());
             var excel = new XSSFWorkbook(byteArrayInputStream)) {
            var sheet = excel.getSheet("Поездки");

            assertThat(sheet).isNotNull();

            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(3); // 1, 2 - заголовок

            var actualRow = sheet.getRow(2);

            var tripWaypoints = objectMapper.readValue(tripWithoutRequests.getWaypoints().data(), new TypeReference<ArrayList<Waypoint>>() {});

            String humanReadableIdFromWaypoints = getHumanReadableIdFromWaypoints(tripWaypoints);
            assertThat(actualRow.getCell(3).getStringCellValue()).isEqualTo(humanReadableIdFromWaypoints);
            int countHumanReadableIdFromWaypoints = humanReadableIdFromWaypoints.split(";").length;
            assertEquals(1, countHumanReadableIdFromWaypoints);

            String firstAddress = getAddressFromWaypoint(tripWaypoints.get(0));
            String lastAddress = getAddressFromWaypoint(tripWaypoints.get(tripWaypoints.size() - 1));
            String middleAddresses = tripWaypoints.stream().skip(1).limit(tripWaypoints.size() - 2L).map(this::getAddressFromWaypoint).collect(Collectors.joining("; "));
            assertThat(actualRow.getCell(6).getStringCellValue()).isEqualTo(firstAddress);
            assertThat(actualRow.getCell(7).getStringCellValue()).isEqualTo(middleAddresses);
            assertThat(actualRow.getCell(8).getStringCellValue()).isEqualTo(lastAddress);
        }
    }

//    @SneakyThrows
//    @Test
//    @DisplayName("Экспорт данных при отсутствии requests и формирование адреса из полей")
//    @Transactional(propagation = Propagation.NOT_SUPPORTED)
//    void exportDataFromWaypointsAndCreateAddressFromFieldsTest() {
//        var contractor = new ContractorsRecord();
//        contractor.setId(UUID.randomUUID());
//        contractor.setDigitId(BigInteger.ONE);
//        contractor.setAutoassign(true);
//
//        dslContext.insertInto(Tables.CONTRACTORS).set(contractor).execute();
//
//        dispatcher = new DispatcherRecord();
//        dispatcher.setId(UUID.randomUUID());
//        dispatcher.setHumanReadableId("DS-0001-0001");
//        dispatcher.setLastName("LastNameDispatcher");
//        dispatcher.setFirstName("FirstNameDispatcher");
//        dispatcher.setPatronymic("PatronymicDispatcher");
//        dispatcher.setPhone("+70327749923");
//        dispatcher.setEmail("disp@mail.ru");
//        dispatcher.setContractorId(contractor.getId());
//
//        dslContext.insertInto(Tables.DISPATCHER).set(dispatcher).execute();
//
//        var requstsEmpty = "[]";
//        var waypointsWithHumanReadableId = """
//                [{"id":"5b1aaf8f-4c64-41fa-9efe-30058fab4eef","latitude":55.787662,"longitude":37.509508,"orderingIndex":1,"country":null,"region":null,"city":null,"street":null,"house":null,"building":null,"waitingTime":null,"address":"г.Краснодар, ул.Селезнева, 136","contacts":[{"contact":{"fullName":null,"phone":"+79998887766"},"requests":[{"humanReadableId":"OT-0025-00001281","type":"LOAD","organization":"ООО Ромашка","cargo":[{"orderingIndex":0,"cargoName":"Коробка","weight":7.5,"volume":90000.0,"occupiedPlacesCount":2,"height":30.0,"length":50.0,"width":60.0,"fragile":true}],"loaders":1,"comment":"Осторожно, не бросать !","pack":[{"name":"Гофрокороб пятислойный (610мм*400мм*330мм)","unit":"штука","count":2}]}]}]},{"id":"5b1aaf8f-4c64-41fa-9efe-30058fab4eef","latitude":55.787662,"longitude":37.509508,"orderingIndex":1,"country":null,"region":null,"city":null,"street":null,"house":null,"building":null,"waitingTime":null,"address":"г.Краснодар, ул.Селезнева, 136","contacts":[{"contact":{"fullName":null,"phone":"+79998887766"},"requests":[{"humanReadableId":"OT-0025-00001281","type":"LOAD","organization":"ООО Ромашка","cargo":[{"orderingIndex":0,"cargoName":"Коробка","weight":7.5,"volume":90000.0,"occupiedPlacesCount":2,"height":30.0,"length":50.0,"width":60.0,"fragile":true}],"loaders":1,"comment":"Осторожно, не бросать !","pack":[{"name":"Гофрокороб пятислойный (610мм*400мм*330мм)","unit":"штука","count":2}]}]}]}]
//                """;
//
//        var driver = new DriverRecord();
//        driver.setId(UUID.randomUUID());
//        driver.setHumanReadableId("DR-0001-0001");
//        driver.setLastName("LastName");
//        driver.setFirstName("FirstName");
//        driver.setPatronymic("Patronymic");
//        driver.setContractorId(contractor.getId());
//        driver.setActive(true);
//        driver.setRating(2);
//        driver.setServing(false);
//        driver.setOnline(false);
//        driver.setContactPhone("+79399912274");
//        driver.setEmail("mail@mail.ru");
//
//        dslContext.insertInto(Tables.DRIVER).set(driver).execute();
//
//        var vehicle = new VehicleRecord();
//        vehicle.setId(UUID.randomUUID());
//        vehicle.setBrand("Brand");
//        vehicle.setModel("Model");
//        vehicle.setColor("Color");
//        vehicle.setContractorId(contractor.getId());
//        vehicle.setDeleted(false);
//        vehicle.setStateNumber("A992AA178RUS");
//
//        dslContext.insertInto(Tables.VEHICLE).set(vehicle).execute();
//
//        tripWithoutRequests = new TripsRecord();
//        tripWithoutRequests.setCreationTime(OffsetDateTime.now(offsetZone));
//        tripWithoutRequests.setId(UUID.randomUUID());
//        tripWithoutRequests.setStartTime(OffsetDateTime.of(LocalDateTime.now(offsetZone), offsetZone));
//        tripWithoutRequests.setEndTime(tripWithoutRequests.getStartTime().plusHours(1));
//        tripWithoutRequests.setStatus(TripStatus.ORDER_FINISHED.name());
//        tripWithoutRequests.setContractorId(contractor.getId());
//        tripWithoutRequests.setWaypoints(JSON.json(waypointsWithHumanReadableId));
//        tripWithoutRequests.setRequests(JSON.json(requstsEmpty));
//        tripWithoutRequests.setDigitId(BigInteger.valueOf(4L));
//        tripWithoutRequests.setFactDistance(10.02);
//        tripWithoutRequests.setCapacity(1.0);
//        tripWithoutRequests.setLoadersWorkTime(23000L);
//        tripWithoutRequests.setDispatcherId(dispatcher.getId());
//        tripWithoutRequests.setRouteHumanReadableId("CT-0001-0004");
//        tripWithoutRequests.setDriverWaitingTime(Duration.ofMinutes(3600).toMillis());
//        tripWithoutRequests.setTimeZone(offsetZone.getId());
//
//        dslContext.insertInto(Tables.TRIPS).set(tripWithoutRequests).execute();
//
//        var driverForTripWithoutRequests = new DriverRecord();
//        driverForTripWithoutRequests.setId(UUID.randomUUID());
//        driverForTripWithoutRequests.setHumanReadableId("DR-0001-0004");
//        driverForTripWithoutRequests.setLastName("LastName");
//        driverForTripWithoutRequests.setFirstName("FirstName");
//        driverForTripWithoutRequests.setPatronymic("Patronymic");
//        driverForTripWithoutRequests.setContractorId(contractor.getId());
//        driverForTripWithoutRequests.setActive(true);
//        driverForTripWithoutRequests.setRating(2);
//        driverForTripWithoutRequests.setServing(false);
//        driverForTripWithoutRequests.setOnline(false);
//        driverForTripWithoutRequests.setContactPhone("+79399912275");
//        driverForTripWithoutRequests.setEmail("mail@mail.ru");
//
//        dslContext.insertInto(Tables.DRIVER).set(driverForTripWithoutRequests).execute();
//
//        var vehicleForTripWithoutRequests = new VehicleRecord();
//        vehicleForTripWithoutRequests.setId(UUID.randomUUID());
//        vehicleForTripWithoutRequests.setBrand("Brand");
//        vehicleForTripWithoutRequests.setModel("Model");
//        vehicleForTripWithoutRequests.setColor("Color");
//        vehicleForTripWithoutRequests.setContractorId(contractor.getId());
//        vehicleForTripWithoutRequests.setDeleted(false);
//        vehicleForTripWithoutRequests.setStateNumber("A996AA178RUS");
//
//        dslContext.insertInto(Tables.VEHICLE).set(vehicleForTripWithoutRequests).execute();
//
//        var shiftForTripWithoutRequests = new ShiftRecord();
//        shiftForTripWithoutRequests.setId(UUID.randomUUID());
//        shiftForTripWithoutRequests.setContractorId(contractor.getId());
//        shiftForTripWithoutRequests.setDriverId(driver.getId());
//        shiftForTripWithoutRequests.setVehicleId(vehicle.getId());
//        shiftForTripWithoutRequests.setStartDate(LocalDateTime.now(offsetZone).minusDays(1));
//        shiftForTripWithoutRequests.setEndDate(LocalDateTime.now(offsetZone).plusDays(1));
//        shiftForTripWithoutRequests.setActive(true);
//        shiftForTripWithoutRequests.setDeleted(false);
//
//        dslContext.insertInto(Tables.SHIFT).set(shiftForTripWithoutRequests).execute();
//
//        driverForTripWithoutRequests.setShiftId(shiftForTripWithoutRequests.getId());
//        driverForTripWithoutRequests.setOnline(true);
//        dslContext.update(Tables.DRIVER)
//                .set(Tables.DRIVER.SHIFT_ID, driverForTripWithoutRequests.getShiftId())
//                .set(Tables.DRIVER.ONLINE, driverForTripWithoutRequests.getOnline())
//                .where(Tables.DRIVER.ID.eq(driverForTripWithoutRequests.getId()))
//                .execute();
//
//        tripWithoutRequests = dslContext.selectFrom(Tables.TRIPS).where(Tables.TRIPS.ID.eq(tripWithoutRequests.getId())).fetchAny();
//        tripWithoutRequests.setDriverId(driver.getId());
//        tripWithoutRequests.setVehicleId(vehicle.getId());
//        tripWithoutRequests.setLoaders(5);
//        dslContext.update(Tables.TRIPS).set(tripWithoutRequests).execute();
//
//        var checkin1ForTripWithoutRequests = new CheckInRecord();
//        checkin1ForTripWithoutRequests.setId(UUID.randomUUID());
//        checkin1ForTripWithoutRequests.setStatus(TripStatus.DRIVER_ARRIVED.name());
//        checkin1ForTripWithoutRequests.setTripId(tripWithoutRequests.getId());
//        checkin1ForTripWithoutRequests.setLatitude(55.6120850000000004);
//        checkin1ForTripWithoutRequests.setLongitude(37.2006659999999982);
//        checkin1ForTripWithoutRequests.setTime(OffsetDateTime.now(offsetZone));
//        checkin1ForTripWithoutRequests.setTimeZone(offsetZone.getId());
//        checkin1ForTripWithoutRequests.setType("AUTO");
//        checkins.add(checkin1ForTripWithoutRequests);
//
//        var checkin3ForTripWithoutRequests = new CheckInRecord();
//        checkin3ForTripWithoutRequests.setId(UUID.randomUUID());
//        checkin3ForTripWithoutRequests.setStatus(TripStatus.ORDER_FINISHED.name());
//        checkin3ForTripWithoutRequests.setTripId(tripWithoutRequests.getId());
//        checkin3ForTripWithoutRequests.setLatitude(55.6120850000000004);
//        checkin3ForTripWithoutRequests.setLongitude(37.2006659999999982);
//        checkin3ForTripWithoutRequests.setTime(OffsetDateTime.now(offsetZone));
//        checkin3ForTripWithoutRequests.setTimeZone(offsetZone.getId());
//        checkin3ForTripWithoutRequests.setType("AUTO");
//        checkins.add(checkin3ForTripWithoutRequests);
//
//        checkins.forEach(checkInRecord -> dslContext.insertInto(Tables.CHECK_IN).set(checkInRecord).execute());
//
//        var filters = """
//                {
//                    "duration" : {
//                        "start":"%s",
//                        "end":"%s"
//                    },
//                    "statuses" : [
//                        "ORDER_FINISHED",
//                        "WAITING_FOR_ASSIGNMENT",
//                        "ORDER_CANCELLED_BY_CLIENT"
//                    ]
//                }
//                """.formatted(OffsetDateTime.now(offsetZone).minusDays(2)
//                ,OffsetDateTime.now(offsetZone).plusDays(2));
//        var content = mockMvc.perform(get("/files/trips/?filters="+Base64.getEncoder().encodeToString(filters.getBytes(StandardCharsets.UTF_8)))
//                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse().getContentAsString();
//
//        var response = new ObjectMapper().readValue(content, new TypeReference<Map<String, String>>() {
//        });
//
//        var file = response.get("result_url");
//        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(3))
//                .until(() -> Files.exists(Path.of(TEMP_DIR, file.substring(0, file.length() - 1) + ".done")));
//
//        var bytes = mockMvc.perform(get(response.get("result_url"))
//                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse().getContentAsByteArray();
//
//        try (var byteArrayInputStream = new ByteArrayInputStream(bytes);
//             var excel = new XSSFWorkbook(byteArrayInputStream)) {
//            var sheet = excel.getSheet("Поездки");
//
//            assertThat(sheet).isNotNull();
//
//            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(3); // 1, 2 - заголовок
//
//            var actualRow = sheet.getRow(2);
//
//            var tripWaypoints = objectMapper.readValue(tripWithoutRequests.getWaypoints().data(), new TypeReference<ArrayList<Waypoint>>() {});
//
//            String humanReadableIdFromWaypoints = getHumanReadableIdFromWaypoints(tripWaypoints);
//            assertThat(actualRow.getCell(3).getStringCellValue()).isEqualTo(humanReadableIdFromWaypoints);
//            int countHumanReadableIdFromWaypoints = humanReadableIdFromWaypoints.split(";").length;
//            assertEquals(1, countHumanReadableIdFromWaypoints);
//
//            String firstAddress = getAddressFromWaypoint(tripWaypoints.getFirst());
//            String lastAddress = getAddressFromWaypoint(tripWaypoints.getLast());
//            String middleAddresses = tripWaypoints.stream().skip(1).limit(tripWaypoints.size() - 2L).map(this::getAddressFromWaypoint).collect(Collectors.joining("; "));
//            assertThat(actualRow.getCell(6).getStringCellValue()).isEqualTo(firstAddress);
//            assertThat(actualRow.getCell(7).getStringCellValue()).isEqualTo(middleAddresses);
//            assertThat(actualRow.getCell(8).getStringCellValue()).isEqualTo(lastAddress);
//        }
//    }

    @SneakyThrows
    @Test
    @DisplayName("Экспорт данных при отсутствии requests и waypoints")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void exportDataFromEmptyRequestsAndWaypoints() {
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


        var requstsEmpty = "[]";
        var waypointsEmpty = "[]";

        var driver = new DriverRecord();
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

        tripWithoutRequests = new TripsRecord();
        tripWithoutRequests.setCreationTime(OffsetDateTime.now(offsetZone));
        tripWithoutRequests.setId(UUID.randomUUID());
        tripWithoutRequests.setStartTime(OffsetDateTime.of(LocalDateTime.now(offsetZone), offsetZone));
        tripWithoutRequests.setEndTime(tripWithoutRequests.getStartTime().plusHours(1));
        tripWithoutRequests.setStatus(TripStatus.ORDER_FINISHED.name());
        tripWithoutRequests.setContractorId(contractor.getId());
        tripWithoutRequests.setWaypoints(JSON.json(waypointsEmpty));
        tripWithoutRequests.setRequests(JSON.json(requstsEmpty));
        tripWithoutRequests.setDigitId(BigInteger.valueOf(4L));
        tripWithoutRequests.setFactDistance(10.02);
        tripWithoutRequests.setCapacity(1.0);
        tripWithoutRequests.setLoadersWorkTime(23000L);
        tripWithoutRequests.setDispatcherId(dispatcher.getId());
        tripWithoutRequests.setRouteHumanReadableId("CT-0001-0004");
        tripWithoutRequests.setDriverWaitingTime(Duration.ofMinutes(3600).toMillis());
        tripWithoutRequests.setTimeZone(offsetZone.getId());

        dslContext.insertInto(Tables.TRIPS).set(tripWithoutRequests).execute();

        var driverForTripWithoutRequests = new DriverRecord();
        driverForTripWithoutRequests.setId(UUID.randomUUID());
        driverForTripWithoutRequests.setHumanReadableId("DR-0001-0004");
        driverForTripWithoutRequests.setLastName("LastName");
        driverForTripWithoutRequests.setFirstName("FirstName");
        driverForTripWithoutRequests.setPatronymic("Patronymic");
        driverForTripWithoutRequests.setContractorId(contractor.getId());
        driverForTripWithoutRequests.setActive(true);
        driverForTripWithoutRequests.setRating(2);
        driverForTripWithoutRequests.setServing(false);
        driverForTripWithoutRequests.setOnline(false);
        driverForTripWithoutRequests.setContactPhone("+79399912275");
        driverForTripWithoutRequests.setEmail("mail@mail.ru");

        dslContext.insertInto(Tables.DRIVER).set(driverForTripWithoutRequests).execute();

        var vehicleForTripWithoutRequests = new VehicleRecord();
        vehicleForTripWithoutRequests.setId(UUID.randomUUID());
        vehicleForTripWithoutRequests.setBrand("Brand");
        vehicleForTripWithoutRequests.setModel("Model");
        vehicleForTripWithoutRequests.setColor("Color");
        vehicleForTripWithoutRequests.setContractorId(contractor.getId());
        vehicleForTripWithoutRequests.setDeleted(false);
        vehicleForTripWithoutRequests.setStateNumber("A996AA178RUS");

        dslContext.insertInto(Tables.VEHICLE).set(vehicleForTripWithoutRequests).execute();

        var shiftForTripWithoutRequests = new ShiftRecord();
        shiftForTripWithoutRequests.setId(UUID.randomUUID());
        shiftForTripWithoutRequests.setContractorId(contractor.getId());
        shiftForTripWithoutRequests.setDriverId(driver.getId());
        shiftForTripWithoutRequests.setVehicleId(vehicle.getId());
        shiftForTripWithoutRequests.setStartDate(LocalDateTime.now(offsetZone).minusDays(1));
        shiftForTripWithoutRequests.setEndDate(LocalDateTime.now(offsetZone).plusDays(1));
        shiftForTripWithoutRequests.setActive(true);
        shiftForTripWithoutRequests.setDeleted(false);

        dslContext.insertInto(Tables.SHIFT).set(shiftForTripWithoutRequests).execute();

        driverForTripWithoutRequests.setShiftId(shiftForTripWithoutRequests.getId());
        driverForTripWithoutRequests.setOnline(true);
        dslContext.update(Tables.DRIVER)
                .set(Tables.DRIVER.SHIFT_ID, driverForTripWithoutRequests.getShiftId())
                .set(Tables.DRIVER.ONLINE, driverForTripWithoutRequests.getOnline())
                .where(Tables.DRIVER.ID.eq(driverForTripWithoutRequests.getId()))
                .execute();

        tripWithoutRequests = dslContext.selectFrom(Tables.TRIPS).where(Tables.TRIPS.ID.eq(tripWithoutRequests.getId())).fetchAny();
        tripWithoutRequests.setDriverId(driver.getId());
        tripWithoutRequests.setVehicleId(vehicle.getId());
        tripWithoutRequests.setLoaders(5);
        dslContext.update(Tables.TRIPS).set(tripWithoutRequests).execute();

        var checkin1ForTripWithoutRequests = new CheckInRecord();
        checkin1ForTripWithoutRequests.setId(UUID.randomUUID());
        checkin1ForTripWithoutRequests.setStatus(TripStatus.DRIVER_ARRIVED.name());
        checkin1ForTripWithoutRequests.setTripId(tripWithoutRequests.getId());
        checkin1ForTripWithoutRequests.setLatitude(55.6120850000000004);
        checkin1ForTripWithoutRequests.setLongitude(37.2006659999999982);
        checkin1ForTripWithoutRequests.setTime(OffsetDateTime.now(offsetZone));
        checkin1ForTripWithoutRequests.setTimeZone(offsetZone.getId());
        checkin1ForTripWithoutRequests.setType("AUTO");
        checkins.add(checkin1ForTripWithoutRequests);

        var checkin3ForTripWithoutRequests = new CheckInRecord();
        checkin3ForTripWithoutRequests.setId(UUID.randomUUID());
        checkin3ForTripWithoutRequests.setStatus(TripStatus.ORDER_FINISHED.name());
        checkin3ForTripWithoutRequests.setTripId(tripWithoutRequests.getId());
        checkin3ForTripWithoutRequests.setLatitude(55.6120850000000004);
        checkin3ForTripWithoutRequests.setLongitude(37.2006659999999982);
        checkin3ForTripWithoutRequests.setTime(OffsetDateTime.now(offsetZone));
        checkin3ForTripWithoutRequests.setTimeZone(offsetZone.getId());
        checkin3ForTripWithoutRequests.setType("AUTO");
        checkins.add(checkin3ForTripWithoutRequests);

        checkins.forEach(checkInRecord -> dslContext.insertInto(Tables.CHECK_IN).set(checkInRecord).execute());

        var filters = """
                {
                    "duration" : {
                        "start":"%s",
                        "end":"%s"
                    },
                    "statuses" : [
                        "ORDER_FINISHED",
                        "WAITING_FOR_ASSIGNMENT",
                        "ORDER_CANCELLED_BY_CLIENT"
                    ]
                }
                """.formatted(OffsetDateTime.now(offsetZone).minusDays(2)
                ,OffsetDateTime.now(offsetZone).plusDays(2));
        var content = mockMvc.perform(get("/files/trips/?filters="+Base64.getEncoder().encodeToString(filters.getBytes(StandardCharsets.UTF_8)))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        var response = new ObjectMapper().readValue(content, new TypeReference<Map<String, String>>() {
        });

        var result = await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(30))
                .until(() -> mockMvc.perform(get(response.get("url") + "/")
                                .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse(), r -> {
                    try {
                        return !r.getContentAsString().contains("in_progress\": true");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                });

        try (var byteArrayInputStream = new ByteArrayInputStream(result.getContentAsByteArray());
             var excel = new XSSFWorkbook(byteArrayInputStream)) {
            var sheet = excel.getSheet("Поездки");

            assertThat(sheet).isNotNull();

            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(3); // 1, 2 - заголовок

            var actualRow = sheet.getRow(2);

            var tripWaypoints = objectMapper.readValue(tripWithoutRequests.getWaypoints().data(), new TypeReference<ArrayList<Waypoint>>() {});

            String humanReadableIdFromWaypoints = getHumanReadableIdFromWaypoints(tripWaypoints);
            assertThat(actualRow.getCell(3).getStringCellValue()).isEqualTo(humanReadableIdFromWaypoints);
            int countHumanReadableIdFromWaypoints = humanReadableIdFromWaypoints.split(";").length;
            assertEquals(1, countHumanReadableIdFromWaypoints);
            assertThat(actualRow.getCell(6).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(7).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(8).getStringCellValue()).isEqualTo("Н/Д");
        }
    }

    private String getAddressFromWaypoint(Waypoint waypoint) {
        return waypoint.address() != null && !waypoint.address().isEmpty() ? waypoint.address() :  mapAddress(waypoint);
    }

    private String getHumanReadableIdFromWaypoints(List<Waypoint> waypointList) {
        if (isNullORBlank(waypointList) || isNullORBlank(waypointList.get(0).contacts())
                || isNullORBlank(waypointList.get(0).contacts().get(0).requests()))
        {
            return "Н/Д";
        }

        return waypointList.stream()
                .flatMap(waypoint -> waypoint.contacts().stream())
                .flatMap(contact -> contact.requests().stream())
                .map(Waypoint.RouteRequest::getHumanReadableId)
                .filter(x -> java.util.Objects.nonNull(x) && !x.isBlank())
                .distinct()
                .collect(Collectors.joining("; "));
    }

    private <T extends List>  boolean isNullORBlank(T value) {
        return value == null || value.isEmpty();
    }

    private String mapAddress(Waypoint waypoint) {
        return String.join(", ", waypoint.region(), waypoint.city(), waypoint.street(), waypoint.house());
    }

    private String mapName(HasName hasName) {
        if (hasName == null) {
            return "Н/Д";
        }
        return String.join(" ", hasName.getFirstName(), hasName.getPatronymic(), String.valueOf(hasName.getLastName().charAt(0))) + ".";
    }

    private String mapFullName(HasName hasName) {
        if (hasName == null) {
            return "Н/Д";
        }
        return String.join(" ", hasName.getFirstName(), hasName.getPatronymic(), hasName.getLastName());
    }

    private String mapStatus(TripStatus status) {
        return switch (status) {
            case ORDER_EXPIRED -> "Поездка просрочена";
            case DRIVER_ARRIVED -> "Водитель прибыл на погрузку";
            case ORDER_FINISHED -> "Заказ выполнен";
            case DRIVER_ASSIGNED -> "Закреплен за водителем";
            case TRIP_IN_PROGRESS -> "Водитель везет груз";
            case INTERMEDIATE_WAYPOINT_ARRIVED -> "Водитель на промежуточной точке";
            case DRIVER_ON_THE_WAY -> "Водитель выехал";
            case SENT_TO_CONTRACTOR -> "Опубликовано в системе исполнителя";
            case WAITING_FOR_ASSIGNMENT -> "Ожидает назначения";
            case ORDER_CANCELLED_BY_CLIENT -> "Поездка отменена клиентом";
            case ORDER_CANCELLED_BY_DRIVER -> "Поездка отменена водителем";
            default -> "Н/Д";
        };
    }

    private String mapCheckinType(CheckinType checkinType){
        return switch (checkinType){
            case AUTO -> "Да";
            case MANUAL -> "Нет";
        };
    }

    private TripsExportDto.Checkin getCheckinData(List<CheckInRecord> checkins, List<Waypoint> waypoints, UUID tripId){
        if(checkins==null||checkins.isEmpty()){
            if(waypoints==null||waypoints.isEmpty()){
                return new TripsExportDto.Checkin(null,null, null);
            }
            return new TripsExportDto.Checkin(waypoints.size(),null, null);
        } else {
            var desiredStatuses = List.of(TripStatus.DRIVER_ARRIVED,
                    TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED, TripStatus.ORDER_FINISHED);
            var filteredCheckins = checkins.stream()
                    .filter(checkin -> checkin.getTripId().equals(tripId) && desiredStatuses.contains(TripStatus.valueOf(checkin.getStatus()))).sorted(Comparator.comparing(CheckInRecord::getTime))
                    .collect(Collectors.toList()); //NOSONAR
            var statuses = new StringBuilder();
            var types = new StringBuilder();
            for (int i = 0; i<filteredCheckins.size(); i++){
                if(i+1==filteredCheckins.size()){
                    statuses.append(i + 1).append(") ").append(mapStatus(TripStatus.valueOf(filteredCheckins.get(i).getStatus())));
                    types.append(i + 1).append(") ").append(mapCheckinType(CheckinType.valueOf(filteredCheckins.get(i).getType())));
                } else {
                    statuses.append(i + 1).append(") ").append(mapStatus(TripStatus.valueOf(filteredCheckins.get(i).getStatus()))).append("\n");
                    types.append(i + 1).append(") ").append(mapCheckinType(CheckinType.valueOf(filteredCheckins.get(i).getType()))).append("\n");
                }
            }
            return new TripsExportDto.Checkin(waypoints.size(), statuses.toString(), types.toString());
        }
    }

    private Duration getTripFactDuration(List<CheckInRecord> checkins){
        if(checkins==null||checkins.isEmpty()){
            return null;
        } else return Duration.between(checkins.get(0).getTime(), checkins.get(checkins.size() - 1).getTime());
    }
}
