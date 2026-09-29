package ru.sber.transport.dispatcher.controller.file_resolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.dispatcher.database.dao.*;
import ru.sber.transport.dispatcher.database.model.*;
import ru.sber.transport.dispatcher.dto.enums.VehicleType;
import ru.sber.transport.dispatcher.testutils.TestAutoparks;
import ru.sber.transport.dispatcher.testutils.TestContractors;
import ru.sber.transport.dispatcher.testutils.TestVehicles;
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.dispatcher.dto.enums.VehicleType.CARGO;
import static ru.sber.transport.dispatcher.dto.enums.VehicleType.PASSENGER;


@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@DisplayName("Проверка импорта/экспорта смен")
@MockitoBean(types = Key.class)
@MockitoBean(types = JwtDecoder.class)
class ShiftFileResolverTest extends KafkaTest {

    private static final String ROLE_DISPATCHER_ROOM_ADMIN = "ROLE_DISPATCHER_ROOM_ADMIN";

    private static final String ROLE_MAIN_DISPATCHER_CONTRACTOR = "ROLE_MAIN_DISPATCHER_CONTRACTOR";

    private static final String ROLE_FEDERAL_DISPATCHER_CONTRACTOR = "ROLE_FEDERAL_DISPATCHER_CONTRACTOR";

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private AutoparkRepository autoparkRepository;

    @Autowired
    private DispatcherRepository dispatcherRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UploadStates uploadStates;

    @Autowired
    private ShiftRepository shiftRepository;

    @MockitoBean
    private AuthorizationManager<?> manager;

    private Contractor contractor;
    private Autopark autopark;
    private Vehicle vehicle;
    private Driver driver;
    private Dispatcher dispatcher;

    @BeforeEach
    void createRepository() {
        contractor = contractorRepository.save(TestContractors.createTestContractor());
        autopark = autoparkRepository.save(TestAutoparks.createTestAutopark(contractor));
        var testVehicle = TestVehicles.createTestVehicle(autopark, 0);
        testVehicle.setStateNumber("X000XX163");
        vehicle = vehicleRepository.save(testVehicle);
        driver = Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), autopark.getContractor())
                .set(Select.field(Driver::getAutopark), autopark)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getPersonnelNumber), "1926473")
                .ignore(Select.field(Driver::getAttributes))
                .create();
        driver = driverRepository.save(driver);

        var dispatcher = new Dispatcher();
        dispatcher.setLastName("Фамилия");
        dispatcher.setFirstName("Имя");
        dispatcher.setPatronymic("Отчество");
        dispatcher.setHumanReadableId("Старое HRI");
        dispatcher.setEmail("your@email.com");
        dispatcher.setPhone("+79000000000");
        dispatcher.setContractor(contractor);
        dispatcher.setAutopark(autopark);
        this.dispatcher = dispatcherRepository.save(dispatcher);

        AuthorizeUtils.authorize(manager, "ROLE_USER");
    }

    @AfterEach
    void clearRepository() {
        shiftRepository.deleteAllInBatch();
        vehicleRepository.deleteAllInBatch();
        driverRepository.deleteAllInBatch();
        dispatcherRepository.deleteAllInBatch();
        autoparkRepository.deleteAllInBatch();
        contractorRepository.deleteAllInBatch();
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка экспорта пустого шаблона")
    void testExportSample() {
        var content = mockMvc.perform(get("/files/shifts")
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        var response = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {
        });

        var result = await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(10))
                .until(() -> mockMvc.perform(get(response.get("result_url") + "/")
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

        var bytes = result.getContentAsByteArray();

        try (var byteArrayInputStream = new ByteArrayInputStream(bytes); var excel = new XSSFWorkbook(byteArrayInputStream)) {
            var sheet = excel.getSheet("Смены");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка импорта автомобилей")
    void testImport() {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/shifts.xlsx"));

        mockMvc.perform(multipart("/files/shifts/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                        .claim("roles", List.of("ROLE_USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString()).size(), equalTo(1));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getFinished(), equalTo(true));

        List<Shift> shifts = shiftRepository.findAll();

        Assertions.assertThat(shifts).hasSize(1);
        Assertions.assertThat(shifts.getFirst().getStartDate()).isEqualTo(LocalDateTime.of(2026, 6, 10, 0,0,0,0));
        Assertions.assertThat(shifts.getFirst().getEndDate()).isEqualTo(LocalDateTime.of(2026, 6, 10, 23,59,59,0));
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка импорта автомобилей, админ")
    void testImportAdmin() {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/shifts.xlsx"));

        mockMvc.perform(multipart("/files/shifts/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                        .claim("roles", List.of(ROLE_DISPATCHER_ROOM_ADMIN, "ROLE_USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString()).size(), equalTo(1));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getFinished(), equalTo(true));

        List<Shift> shifts = shiftRepository.findAll();

        Assertions.assertThat(shifts).hasSize(1);
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка импорта автомобилей, федеральный диспетчер")
    void testImportFederalDispatcher() {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/shifts.xlsx"));

        mockMvc.perform(multipart("/files/shifts/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                        .claim("roles", List.of(ROLE_FEDERAL_DISPATCHER_CONTRACTOR, "ROLE_USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString()).size(), equalTo(1));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getFinished(), equalTo(true));

        List<Shift> shifts = shiftRepository.findAll();

        Assertions.assertThat(shifts).hasSize(1);
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка импорта автомобилей, менеджер")
    void testImportManager() {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/shifts.xlsx"));

        mockMvc.perform(multipart("/files/shifts/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                        .claim("roles", List.of(ROLE_MAIN_DISPATCHER_CONTRACTOR, "ROLE_USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString()).size(), equalTo(1));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getFinished(), equalTo(true));

        List<Shift> shifts = shiftRepository.findAll();

        Assertions.assertThat(shifts).hasSize(1);
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка импорта автомобилей, авто не найдено")
    void testImportVehicleNotFound() {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/shifts.xlsx"));

        vehicle.setStateNumber("A111AA23");
        vehicleRepository.save(vehicle);

        mockMvc.perform(multipart("/files/shifts/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                        .claim("roles", List.of("ROLE_USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString()).size(), equalTo(1));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getFinished(), equalTo(true));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getPages().getFirst().getExceptionStrings().getFirst(), equalTo("Автомобиль X000XX163 не найден"));

        List<Shift> shifts = shiftRepository.findAll();

        Assertions.assertThat(shifts).hasSize(0);
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка импорта автомобилей, водитель не найден")
    void testImportDriverNotFound() {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/shifts.xlsx"));

        driver.setPersonnelNumber("125123");
        driverRepository.save(driver);

        mockMvc.perform(multipart("/files/shifts/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                        .claim("roles", List.of("ROLE_USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString()).size(), equalTo(1));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getFinished(), equalTo(true));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getPages().getFirst().getExceptionStrings().getFirst(), equalTo("Водитель 1926473 не найден"));

        List<Shift> shifts = shiftRepository.findAll();

        Assertions.assertThat(shifts).hasSize(0);
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка импорта автомобилей, диспетчер не закреплен за филиалом")
    void testImportDispatcherAutoparkIdIsNUll() {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/shifts.xlsx"));

        dispatcher.setAutopark(null);
        dispatcherRepository.save(dispatcher);

        mockMvc.perform(multipart("/files/shifts/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                        .claim("roles", List.of("ROLE_USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString()).size(), equalTo(1));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getFinished(), equalTo(true));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getPages().getFirst().getExceptionStrings().getFirst(), equalTo("Пользователь не принадлежит филиалу контрагента"));

        List<Shift> shifts = shiftRepository.findAll();

        Assertions.assertThat(shifts).hasSize(0);
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка импорта автомобилей, водитель занят")
    void testImportDispatcherDriverIsBusy() {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/shifts.xlsx"));

        var shift = new Shift();
        shift.setDriver(driver);
        shift.setVehicle(vehicleRepository.save(TestVehicles.createTestVehicle(autopark, 1)));
        shift.setContractorId(contractor.getId());
        shift.setStartDate(LocalDateTime.of(2026, 6, 10, 0,0,0,0));
        shift.setEndDate(LocalDateTime.of(2026, 6, 10, 23,0,0,0));
        shiftRepository.save(shift);

        mockMvc.perform(multipart("/files/shifts/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                        .claim("roles", List.of("ROLE_USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString()).size(), equalTo(1));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getFinished(), equalTo(true));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getPages().getFirst().getExceptionStrings().getFirst(), equalTo("На водителя 1926473 уже существует активная смена"));

        List<Shift> shifts = shiftRepository.findAll();

        Assertions.assertThat(shifts).hasSize(1);
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка импорта автомобилей, авто занято")
    void testImportDispatcherVehicleIsBusy() {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/shifts.xlsx"));

        var newDriver = Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), autopark.getContractor())
                .set(Select.field(Driver::getAutopark), autopark)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getPersonnelNumber), "837483")
                .ignore(Select.field(Driver::getAttributes))
                .create();
        driverRepository.save(newDriver);

        var shift = new Shift();
        shift.setDriver(newDriver);
        shift.setVehicle(vehicle);
        shift.setContractorId(contractor.getId());
        shift.setStartDate(LocalDateTime.of(2026, 6, 10, 0,0,0,0));
        shift.setEndDate(LocalDateTime.of(2026, 6, 10, 23,0,0,0));
        shiftRepository.save(shift);

        mockMvc.perform(multipart("/files/shifts/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                        .claim("roles", List.of("ROLE_USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString()).size(), equalTo(1));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getFinished(), equalTo(true));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getPages().getFirst().getExceptionStrings().getFirst(), equalTo("На автомобиль X000XX163 уже существует активная смена"));

        List<Shift> shifts = shiftRepository.findAll();

        Assertions.assertThat(shifts).hasSize(1);
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка импорта автомобилей, авто и водитель заняты")
    void testImportDispatcherVehicleAndDriverAreBusy() {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/shifts.xlsx"));

        var shift = new Shift();
        shift.setDriver(driver);
        shift.setVehicle(vehicle);
        shift.setContractorId(contractor.getId());
        shift.setStartDate(LocalDateTime.of(2026, 6, 10, 0,0,0,0));
        shift.setEndDate(LocalDateTime.of(2026, 6, 10, 23,0,0,0));
        shiftRepository.save(shift);

        mockMvc.perform(multipart("/files/shifts/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                        .claim("roles", List.of("ROLE_USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString()).size(), equalTo(1));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getFinished(), equalTo(true));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getPages().getFirst().getExceptionStrings().getFirst(), equalTo("На автомобиль X000XX163 и водителя 1926473 уже существует активная смена"));

        List<Shift> shifts = shiftRepository.findAll();

        Assertions.assertThat(shifts).hasSize(1);
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка импорта автомобилей, диспетчер не принадлежит филиалу водителя")
    void testImportDriverAutoparkIsDifferent() {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/shifts.xlsx"));

        var newAutopark = TestAutoparks.createTestAutopark(contractor);
        newAutopark.setName("newAutopark");
        newAutopark = autoparkRepository.save(newAutopark);
        driver.setAutopark(newAutopark);
        driverRepository.save(driver);

        mockMvc.perform(multipart("/files/shifts/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                        .claim("roles", List.of("ROLE_USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString()).size(), equalTo(1));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getFinished(), equalTo(true));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getPages().getFirst().getExceptionStrings().getFirst(), equalTo("Водитель не принадлежит филиалу пользователя"));

        List<Shift> shifts = shiftRepository.findAll();

        Assertions.assertThat(shifts).hasSize(0);
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка импорта автомобилей, диспетчер не принадлежит филиалу автомобиля")
    void testImportVehicleAutoparkIsDifferent() {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/shifts.xlsx"));

        var newAutopark = TestAutoparks.createTestAutopark(contractor);
        newAutopark.setName("newAutopark");
        newAutopark = autoparkRepository.save(newAutopark);
        vehicle.setAutopark(newAutopark);
        vehicleRepository.save(vehicle);

        mockMvc.perform(multipart("/files/shifts/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                        .claim("roles", List.of("ROLE_USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString()).size(), equalTo(1));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getFinished(), equalTo(true));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("shifts", dispatcher.getId().toString())
                        .getFirst().getPages().getFirst().getExceptionStrings().getFirst(), equalTo("Автомобиль не принадлежит филиалу пользователя"));

        List<Shift> shifts = shiftRepository.findAll();

        Assertions.assertThat(shifts).hasSize(0);
    }
}

