package ru.sber.transport.trip.file_resolvers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.ConsentFunction;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.trip.business.dto.CheckinType;
import ru.sber.transport.trip.business.dto.Prefix;
import ru.sber.transport.trip.business.dto.TripsExportDto;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.*;
import ru.sber.transport.trip.dto.IntegrationRequestDTOTest;
import ru.sber.transport.trip.messaging.providers.ContractorProvider;
import ru.sber.transport.trip.messaging.providers.DispatcherProvider;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.messaging.providers.VehicleProvider;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trip.providers.history.trip.mapper.TripHistoryMapper;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
@MockBean(Key.class)
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
public class TripsResolverTest extends KafkaTest {

    public static final String TEMP_DIR = "application/target/test/files";

    @MockBean
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

    @MockBean
    private ConsentFunction function;

    @Autowired
    private TripProvider tripProvider;

    @Autowired
    private UploadStates uploadStates;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorizationManager<?> manager;

    @Autowired
    private DSLContext dslContext;

    private DispatcherRecord dispatcher;

    private TripsRecord trip1;

    private TripsRecord trip2;

    private final List<CheckInRecord> checkins = new ArrayList<>();

    private TripHistoryItem tripHistoryItemDispatcherTakeToWork;

    private TripHistoryItem tripHistoryItemStatusChanged;

    @Autowired
    private TripHistoryMapper tripHistoryMapper;

    @BeforeEach
    void setup() {
        when(function.apply(any())).thenReturn(true);

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

        var requests = "[{\"id\":\"63f0d17c-3b88-4500-becd-1a90f04494ab\",\"authorId\":\"30d6bac3-c1bc-4ce3-a6a4-e15f3d489914\",\"humanReadableId\":\"OT-0001-00009505\",\"author\":{\"id\":null,\"lastName\":\"Бландин\",\"firstName\":\"Александр\",\"patronymic\":\"Дмитриевич\",\"mobilePhone\":\"+78001307001\",\"email\":null,\"organization\":null},\"passengerId\":\"30d6bac3-c1bc-4ce3-a6a4-e15f3d489914\",\"passenger\":{\"id\":null,\"lastName\":\"Бландин\",\"firstName\":\"Александр\",\"patronymic\":\"Дмитриевич\",\"mobilePhone\":\"+78001307001\",\"email\":null,\"organization\":null},\"taxiClass\":\"ECONOMY\",\"passengerCount\":1,\"expected\":{\"cost\":1335.0,\"distance\":11.214,\"time\":1217.000000000},\"creationTime\":\"2023-10-23T10:45:12.000000307+00:00\",\"desiredDate\":\"2023-10-23T10:50:12.000000172+00:00\",\"rideId\":null,\"suburb\":false,\"timeZone\":\"GMT+03\",\"requestOptions\":null,\"tariffId\":\"11d54b73-11e4-4b37-af22-1a24173e1f04\",\"contractorId\":\"108ce2a2-c054-4dbf-9f23-34499dd69a59\",\"status\":\"TAXI_TRIP_FINISHED\",\"comment\":null,\"waypoints\":[{\"id\":\"42366678-6583-449e-816a-bf561cf4b2ba\",\"latitude\":55.75198989085822,\"longitude\":37.60040860924344,\"orderingIndex\":0,\"country\":\"Россия\",\"region\":\"Москва\",\"city\":\"Москва\",\"street\":\"Арбат\",\"house\":\"1\",\"building\":null,\"waitingTime\":\"PT0S\"},{\"id\":\"aad7aaa6-e853-4fbd-9189-d51fcbb6df07\",\"latitude\":55.74339357458361,\"longitude\":37.54573687155735,\"orderingIndex\":1,\"country\":\"Россия\",\"region\":\"Москва\",\"city\":\"Москва\",\"street\":\"Дунаевского\",\"house\":\"1\",\"building\":null,\"waitingTime\":\"PT0S\"}],\"driverWaitingTime\":\"PT1M\",\"factDistance\":2.0,\"transportType\":\"TAXI\",\"coop\":false}]";
        var waypoints = "[{\"id\":\"a4b63256-146b-45ad-a9fc-496318a5dd89\",\"latitude\":55.6120850000000004,\"longitude\":37.2006659999999982,\"orderingIndex\":1,\"country\":\"Россия\",\"region\":\"Москва\",\"city\":\"Москва\",\"street\":\"улица Ленина\",\"house\":\"5\",\"building\":null,\"waitingTime\":20,\"contact\":{\"phone\":\"+71111111111\",\"name\":\"Николай Вишняков\"}},{\"id\":\"11401c3b-e37e-4261-9f01-55da3316d7e5\",\"latitude\":55.779477,\"longitude\":37.6444290000000024,\"orderingIndex\":2,\"country\":\"Россия\",\"region\":\"Москва\",\"city\":\"Москва\",\"street\":\"Протопоповский переулок\",\"house\":\"40\",\"building\":null,\"waitingTime\":20,\"contact\":{\"phone\":\"+71111111121\",\"name\":\"Инга Пантелеевна\"}}]";

        trip1 = new TripsRecord();
        trip1.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip1.setId(UUID.randomUUID());
        trip1.setFactStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        trip1.setFactEndTime(trip1.getFactStartTime().plusHours(1));
        trip1.setExpectedStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        trip1.setExpectedEndTime(trip1.getExpectedStartTime().plusHours(1));
        trip1.setStatus(TripStatus.ORDER_FINISHED.name());
        trip1.setContractorId(contractor.getId());
        trip1.setWaypoints(JSON.json(waypoints));
        trip1.setRequests(JSON.json("[]"));
        trip1.setDigitId(BigInteger.ONE);
        trip1.setFactDistance(10.02);
        trip1.setPassengerCount(2);
        trip1.setTaxiClass(TaxiClass.ECONOMY.name());
        trip1.setDriverWaitingTime(2300L);
        trip1.setDispatcherId(dispatcher.getId());
        trip1.setExpectedCost(1000L);
        trip1.setExpectedDistance(100.0);
        trip1.setExpectedTime(1345L);
        trip1.setTimeZone("GMT+03");
        trip1.setExternalHumanReadableId("TT-OT-0001-0001");
        trip1.setComment("Comment");
        trip1.setReportCreated(false);

        dslContext.insertInto(Tables.TRIPS_).set(trip1).execute();

        trip2 = new TripsRecord();
        trip2.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip2.setId(UUID.randomUUID());
        trip2.setFactStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        trip2.setFactEndTime(trip2.getFactStartTime().plusHours(1));
        trip2.setExpectedStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        trip2.setExpectedEndTime(trip2.getExpectedStartTime().plusHours(1));
        trip2.setStatus(TripStatus.ORDER_FINISHED.name());
        trip2.setContractorId(contractor.getId());
        trip2.setWaypoints(JSON.json(waypoints));
        trip2.setRequests(JSON.json(requests));
        trip2.setDigitId(BigInteger.TWO);
        trip2.setFactDistance(10.02);
        trip2.setPassengerCount(1);
        trip2.setTaxiClass("GROUP_TRANSFER");
        trip2.setDriverWaitingTime(2300L);
        trip2.setDispatcherId(dispatcher.getId());
        trip2.setExpectedCost(1000L);
        trip2.setExpectedDistance(100.0);
        trip2.setExpectedTime(1345L);
        trip2.setReportCreated(false);
        trip2.setTimeZone("GMT+03");

        dslContext.insertInto(Tables.TRIPS_).set(trip2).execute();

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
        shift.setStartDate(LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
        shift.setEndDate(LocalDateTime.now(ZoneOffset.UTC).plusDays(1));
        shift.setActive(true);
        shift.setDeleted(false);

        dslContext.insertInto(Tables.SHIFT).set(shift).execute();

        driver.setShiftId(shift.getId());
        driver.setOnline(true);
        dslContext.update(Tables.DRIVER).set(driver).execute();

        trip1 = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip1.getId())).fetchAny();
        trip1.setDriverId(driver.getId());
        trip1.setVehicleId(vehicle.getId());
        dslContext.update(Tables.TRIPS_).set(trip1).execute();

        trip2 = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.ID.eq(trip2.getId())).fetchAny();
        trip2.setDriverId(driver.getId());
        trip2.setVehicleId(vehicle.getId());
        dslContext.update(Tables.TRIPS_).set(trip2).execute();

        tripHistoryItemDispatcherTakeToWork = Instancio.of(TripHistoryItem.class)
                .set(Select.field(TripHistoryItem::getTripId), trip1.getId())
                .set(Select.field(TripHistoryItem::getOldDispatcherId), null)
                .set(Select.field(TripHistoryItem::getNewDispatcherId), null)
                .set(Select.field(TripHistoryItem::getNewPlannedShiftId), null)
                .set(Select.field(TripHistoryItem::getOldPlannedShiftId), null)
                .set(Select.field(TripHistoryItem::getAction), ActionType.DISPATCHER_TAKE_TO_WORK)
                .set(Select.field(TripHistoryItem::getChangeTime), LocalDateTime.now(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).withNano(0).withSecond(0))
                .create();
        tripHistoryItemStatusChanged = Instancio.of(TripHistoryItem.class)
                .set(Select.field(TripHistoryItem::getTripId), trip1.getId())
                .set(Select.field(TripHistoryItem::getOldDispatcherId), null)
                .set(Select.field(TripHistoryItem::getNewDispatcherId), null)
                .set(Select.field(TripHistoryItem::getNewPlannedShiftId), null)
                .set(Select.field(TripHistoryItem::getOldPlannedShiftId), null)
                .set(Select.field(TripHistoryItem::getAction), ActionType.STATUS_CHANGING)
                .set(Select.field(TripHistoryItem::getChangeTime), LocalDateTime.now(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).withNano(0).withSecond(0).plusMinutes(1))
                .create();
        dslContext.insertInto(Tables.TRIP_HISTORY).set(tripHistoryMapper.toRecord(tripHistoryItemDispatcherTakeToWork)).execute();
        dslContext.insertInto(Tables.TRIP_HISTORY).set(tripHistoryMapper.toRecord(tripHistoryItemStatusChanged)).execute();

        var checkin1 = new CheckInRecord();
        checkin1.setId(UUID.randomUUID());
        checkin1.setStatus(TripStatus.DRIVER_ARRIVED.name());
        checkin1.setTripId(trip1.getId());
        checkin1.setLatitude(55.6120850000000004);
        checkin1.setLongitude(37.2006659999999982);
        checkin1.setTime(OffsetDateTime.now());
        checkin1.setTimeZone("GMT+03");
        checkin1.setType("AUTO");
        checkins.add(checkin1);

        var checkin2 = new CheckInRecord();
        checkin2.setId(UUID.randomUUID());
        checkin2.setStatus(TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED.name());
        checkin2.setTripId(trip1.getId());
        checkin2.setLatitude(55.6120850000000004);
        checkin2.setLongitude(37.2006659999999982);
        checkin2.setTime(OffsetDateTime.now());
        checkin2.setTimeZone("GMT+03");
        checkin2.setType("MANUAL");
        checkins.add(checkin2);

        var checkin3 = new CheckInRecord();
        checkin3.setId(UUID.randomUUID());
        checkin3.setStatus(TripStatus.ORDER_FINISHED.name());
        checkin3.setTripId(trip1.getId());
        checkin3.setLatitude(55.6120850000000004);
        checkin3.setLongitude(37.2006659999999982);
        checkin3.setTime(OffsetDateTime.now());
        checkin3.setTimeZone("GMT+03");
        checkin3.setType("AUTO");
        checkins.add(checkin3);

        checkins.forEach(checkInRecord -> dslContext.insertInto(Tables.CHECK_IN).set(checkInRecord).execute());

       new File(TEMP_DIR).mkdir();
       System.setProperty("java.io.tmpdir", TEMP_DIR);
    }

    @SneakyThrows
    @Test
    @DisplayName("Экспорт")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void exportData() {
        var filters = """
                {
                    "duration" : {
                        "start":"%s",
                        "end":"%s"
                    },
                    "statuses" : [
                        "ORDER_FINISHED",
                        "TRIP_IN_PROGRESS"
                    ]
                }
                """.formatted(OffsetDateTime.now(ZoneOffset.UTC).minusDays(2)
                        ,OffsetDateTime.now(ZoneOffset.UTC).plusDays(2));
        var content = mockMvc.perform(get("/files/trips/?filters="+Base64.getEncoder().encodeToString(filters.getBytes(StandardCharsets.UTF_8)))
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        var response = new ObjectMapper().readValue(content, new TypeReference<Map<String, String>>() {
        });

        var result = await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(10))
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

            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(3); // 1 - заголовок

            var cellIndex = 0;
            var row = sheet.getRow(0);
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("№");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("ID поездки");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("ID заявки(-ок)");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Дата создания заявки");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Время создания заявки");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Желаемая дата начала поездки");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Желаемое время начала поездки");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Фактическая дата начала поездки");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Фактическое время начала поездки");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Дата просмотра заявки диспетчером");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Время просмотра заявки диспетчером");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Дата изменения статуса заявки диспетчером");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Время изменения статуса заявки диспетчером");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Адрес подачи");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Промежуточные адреса");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Адрес завершения");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Плановое количество точек маршрута");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Хронология обработки статусов Водителем в приложении");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Проверка геопозиции при прохождении точек маршрута водителем. Да - был на точке. Нет - не было на точке.");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Статус поездки");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Комментарии для водителя");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Пассажир поездки");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Номер автомобиля");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Водитель");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Плановая протяженность (км)");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Плановое время поездки (мин)");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Плановое ожидание на точках (мин)");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Плановая стоимость (руб)");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Фактическая протяженность (км)");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Фактическое время поездки (мин)");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Фактическое время ожидания (мин)");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Тип поездки");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Диспетчер");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Телефон диспетчера");
            Assertions.assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Класс авто");
            Assertions.assertThat(row.getCell(cellIndex).getStringCellValue()).isEqualTo("Кол-во пассажиров");

            var actualRow = sheet.getRow(1);

            var tripWaypoints = objectMapper.readValue(trip1.getWaypoints().data(), new TypeReference<ArrayList<Waypoint>>() {});

            var requests = objectMapper.readValue(trip1.getRequests().data(), new TypeReference<ArrayList<Request>>() {});
            var authors = requests.stream().map(Request::getAuthor).toList();
            var waypointList = new LinkedList<>(tripWaypoints.stream().sorted(Comparator.comparing(Waypoint::orderingIndex)).toList());
            var driverId = trip1.getDriverId();
            var checkinData = getCheckinData(checkins, waypointList);
            var commonWaitTime = waypointList.stream().map(Waypoint::waitingTime).reduce(Duration.ZERO, Duration::plus);
            var tripDuration = getTripFactDuration(checkins);
            cellIndex = 0;
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(1);
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("%s-%04d-%08d".formatted(Prefix.TP, contractorProvider.getContractorDigitId(trip1.getContractorId()), trip1.getDigitId()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(trip1.getExternalHumanReadableId());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(DateUtil.getExcelDate(trip1.getCreationTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalDate()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(trip1.getCreationTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(DateUtil.getExcelDate(trip1.getExpectedStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalDate()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(trip1.getExpectedStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(DateUtil.getExcelDate(trip1.getFactStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalDate()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(trip1.getFactStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(DateUtil.getExcelDate(tripHistoryItemDispatcherTakeToWork.getChangeTime().toLocalDate()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(tripHistoryItemDispatcherTakeToWork.getChangeTime().toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(DateUtil.getExcelDate(tripHistoryItemStatusChanged.getChangeTime().toLocalDate()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(tripHistoryItemStatusChanged.getChangeTime().toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapAddress(waypointList.getFirst()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(waypointList.stream().skip(1).limit(waypointList.size() - 2).map(this::mapAddress).collect(Collectors.joining("; ")));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapAddress(waypointList.getLast()));
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(checkinData.quantity().intValue());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(checkinData.status());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(checkinData.waypointConfirmation());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapStatus(TripStatus.valueOf(trip1.getStatus())));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(trip1.getComment());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Инга Пантелеевна; Николай Вишняков");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(vehicleProvider.get(trip1.getVehicleId()).get().getStateNumber());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapFullName(driverProvider.get(driverId).get()));
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(100.0);
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(Duration.ofSeconds(trip1.getExpectedTime()).toMinutes());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(commonWaitTime.toMinutes());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(10);
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(trip1.getFactDistance());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(tripDuration.toMinutes());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(Duration.ofMillis(trip1.getDriverWaitingTime()).toMinutes());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Совместная");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapName(dispatcherProvider.get(trip1.getDispatcherId()).get()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(dispatcherProvider.get(trip1.getDispatcherId()).get().getPhone());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(TaxiClass.valueOf(trip1.getTaxiClass()).getRusName());
            assertThat(actualRow.getCell(cellIndex).getNumericCellValue()).isEqualTo(trip1.getPassengerCount().intValue());

            actualRow = sheet.getRow(2);
            tripWaypoints = objectMapper.readValue(trip2.getWaypoints().data(), new TypeReference<ArrayList<Waypoint>>() {});

            requests = objectMapper.readValue(trip2.getRequests().data(), new TypeReference<ArrayList<Request>>() {});
            authors = requests.stream().map(Request::getAuthor).toList();
            waypointList = new LinkedList<>(tripWaypoints.stream().sorted(Comparator.comparing(Waypoint::orderingIndex)).toList());
            driverId = trip2.getDriverId();
            checkinData = getCheckinData(checkins, waypointList);
            commonWaitTime = waypointList.stream().map(Waypoint::waitingTime).reduce(Duration.ZERO, Duration::plus);
            tripDuration = getTripFactDuration(checkins);

            cellIndex = 0;
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(2);
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("%s-%04d-%08d".formatted(Prefix.TP, contractorProvider.getContractorDigitId(trip2.getContractorId()), trip2.getDigitId()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(requests.stream().map(Request::getHumanReadableId).collect(Collectors.joining("; ")));
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(DateUtil.getExcelDate(trip2.getCreationTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalDate()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(trip2.getCreationTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(DateUtil.getExcelDate(trip2.getExpectedStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalDate()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(trip2.getExpectedStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(DateUtil.getExcelDate(trip2.getFactStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalDate()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(trip2.getFactStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapAddress(waypointList.getFirst()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(waypointList.stream().skip(1).limit(waypointList.size() - 2).map(this::mapAddress).collect(Collectors.joining("; ")));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapAddress(waypointList.getLast()));
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(checkinData.quantity().intValue());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapStatus(TripStatus.valueOf(trip2.getStatus())));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(requests.stream().map(Request::getComment).filter(Objects::nonNull).collect(Collectors.joining("; ")));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(requests.stream().map(Request.class::cast).map(Request::getPassenger).map(this::mapName).map(this::mapNotNull).collect(Collectors.joining("; ")));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(vehicleProvider.get(trip2.getVehicleId()).get().getStateNumber());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapFullName(driverProvider.get(driverId).get()));
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(100.0);
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(Duration.ofSeconds(trip2.getExpectedTime()).toMinutes());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(commonWaitTime.toMinutes());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(10);
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(trip2.getFactDistance());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(Duration.ofMillis(trip2.getDriverWaitingTime()).toMinutes());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Индивидуальная");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapName(dispatcherProvider.get(trip2.getDispatcherId()).get()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(dispatcherProvider.get(trip2.getDispatcherId()).get().getPhone());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Групповой трансфер");
            assertThat(actualRow.getCell(cellIndex).getNumericCellValue()).isEqualTo(trip2.getPassengerCount().intValue());
        }
    }

    private String mapAddress(Waypoint waypoint) {
        return String.join(", ", waypoint.country(), waypoint.region(), waypoint.city(), waypoint.street(), waypoint.house());
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
            case DRIVER_ARRIVED -> "Водитель прибыл в пункт отправления";
            case ORDER_FINISHED -> "Поездка завершена";
            case DRIVER_ASSIGNED -> "Водитель назначен";
            case TRIP_IN_PROGRESS -> "Водитель везет клиента";
            case INTERMEDIATE_WAYPOINT_ARRIVED -> "Остановка в промежуточной точке маршрута";
            case DRIVER_ON_THE_WAY -> "Водитель в пути";
            case SENT_TO_CONTRACTOR -> "Отправлена перевозчику";
            case WAITING_FOR_ASSIGNMENT -> "Ожидает назначения водителя";
            case ORDER_CANCELLED_BY_CLIENT -> "Отменена клиентом";
            case ORDER_CANCELLED_BY_DRIVER -> "Отменена водителем";
            default -> "Н/Д";
        };
    }

    private String mapNotNull(String source) {
        if (source == null) {
            return "Н/Д";
        }
        return source;
    }

    private String mapCheckinType(CheckinType checkinType){
        return switch (checkinType){
            case AUTO -> "Да";
            case MANUAL -> "Нет";
        };
    }

    private TripsExportDto.Checkin getCheckinData(List<CheckInRecord> checkins, List<Waypoint> waypoints){
        if(checkins==null||checkins.isEmpty()){
            if(waypoints==null||waypoints.isEmpty()){
                return new TripsExportDto.Checkin(null,null, null);
            }
            return new TripsExportDto.Checkin(waypoints.size(),null, null);
        } else {
            var desiredStatuses = List.of(TripStatus.DRIVER_ARRIVED,
                    TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED, TripStatus.ORDER_FINISHED);
            var filteredCheckins = checkins.stream()
                    .filter(checkin -> desiredStatuses.contains(TripStatus.valueOf(checkin.getStatus()))).sorted(Comparator.comparing(CheckInRecord::getTime))
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

    private static class Progress{
         private Boolean in_progress;
    }
}
