package ru.sber.transport.trips.cargo.file_resolvers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.trips.cargo.business.dto.CheckinType;
import ru.sber.transport.trips.cargo.business.dto.Prefix;
import ru.sber.transport.trips.cargo.business.dto.TripsExportDto;
import ru.sber.transport.trips.cargo.business.model.CargoRequest;
import ru.sber.transport.trips.cargo.business.model.HasName;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.business.model.Waypoint;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
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
public class CargoReportResolverTest extends KafkaTest {

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

    private final List<CheckInRecord> checkins = new ArrayList<>();

    private static final List<String> ORDERED_FIELDS = ImmutableList.<String>builder()
            .add("№ п/п")
            .add("ФИО водителя")
            .add("№ автомобиля")
            .add("HRID Маршрутного листа")
            .add("Дата подачи ТС")
            .add("Адрес подачи ТС")
            .add("Промежуточные адреса - точки маршрута")
            .add("Адрес последнего пункта назначения")
            .add("Время начала работы (подачи) ТС")
            .add("Время окончания работы ТС")
            .add("Протяженность маршрута, км")
            .add("Ожидание, мин.")
            .add("Погрузочно-разгрузочные работы, ч.")
            .add("Вес (кг)")
            .add("Грузоподъемность ТС, тонн")
            .add("Тариф, руб./км (без НДС)")
            .add("Тариф за время ожидания, руб./м. (без НДС)")
            .add("Тариф на погрузо-разгрузочные работы, руб./ч. (без НДС)")
            .add("Тариф на погрузо-разгрузочные работы, руб./мин. (без НДС)")
            .add("Транспортные расходы без НДС, руб.")
            .add("Итого без НДС, руб.")
            .build();

    @BeforeEach
    void setup() {
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
        trip1.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip1.setId(UUID.randomUUID());
        trip1.setStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        trip1.setEndTime(trip1.getStartTime().plusHours(1));
        trip1.setFinishTime(trip1.getStartTime().plusHours(1));
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
        trip2.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip2.setId(UUID.randomUUID());
        trip2.setStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
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
        trip3.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip3.setId(UUID.randomUUID());
        trip3.setStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
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
        shift.setStartDate(LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
        shift.setEndDate(LocalDateTime.now(ZoneOffset.UTC).plusDays(1));
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
                    }
                }
                """.formatted(OffsetDateTime.now(ZoneOffset.UTC).minusDays(2)
                , OffsetDateTime.now(ZoneOffset.UTC).plusDays(2));
        var content = mockMvc.perform(get("/files/cargo/?filters=" + Base64.getEncoder().encodeToString(filters.getBytes(StandardCharsets.UTF_8)))
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
            var sheet = excel.getSheetAt(0);

            assertThat(sheet).isNotNull();

            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2); // 1, 2 - заголовок

            var cellIndex = 0;
            var row = sheet.getRow(0);

            assertThat(row.getPhysicalNumberOfCells())
                    .isEqualTo(ORDERED_FIELDS.size());

            for (int i = 0; i < ORDERED_FIELDS.size(); i++) {
                assertThat(row.getCell(i).getStringCellValue()).isEqualTo(ORDERED_FIELDS.get(i));
            }

            var actualRow = sheet.getRow(1);

            var tripWaypoints = objectMapper.readValue(trip1.getWaypoints().data(), new TypeReference<ArrayList<Waypoint>>() {
            });

            var requests = objectMapper.readValue(trip1.getRequests().data(), new TypeReference<ArrayList<CargoRequest>>() {
            });
            var waypointList = new LinkedList<>(tripWaypoints.stream().sorted(Comparator.comparing(Waypoint::orderingIndex)).toList());
            var driverId = trip1.getDriverId();
            var vehicle = vehicleProvider.get(trip1.getVehicleId());

            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(1);
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapFullName(driverProvider.get(driverId).get()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(vehicle.get().getStateNumber());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(trip1.getRouteHumanReadableId());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapAddress(waypointList.getFirst()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(waypointList.stream().skip(1).limit(waypointList.size() - 2).map(this::mapAddress).collect(Collectors.joining("; ")));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(mapAddress(waypointList.getLast()));
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(DateUtil.getExcelDate(trip1.getStartTime().toLocalDateTime()));
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(DateUtil.getExcelDate(trip1.getFinishTime().toLocalDateTime()));
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(trip1.getFactDistance());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(Optional.ofNullable(trip1.getDriverWaitingTime()).map(t -> t / (double)60_000).orElse(null));
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(0);
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(requests.stream()
                    .filter(c -> c.getWeight() != null)
                    .mapToDouble(CargoRequest::getWeight)
                    .sum());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(trip1.getCapacity());

            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo("Н/Д");
        }
    }

    private String mapAddress(Waypoint waypoint) {
        return String.join(", ", waypoint.region(), waypoint.city(), waypoint.street(), waypoint.house());
    }

    private String mapFullName(HasName hasName) {
        if (hasName == null) {
            return "Н/Д";
        }
        return String.join(" ", hasName.getLastName(), hasName.getFirstName(), hasName.getPatronymic());
    }
}
