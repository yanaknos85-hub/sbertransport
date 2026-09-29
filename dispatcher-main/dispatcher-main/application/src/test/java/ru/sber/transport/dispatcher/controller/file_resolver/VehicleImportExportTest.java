package ru.sber.transport.dispatcher.controller.file_resolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.dispatcher.database.dao.AutoparkRepository;
import ru.sber.transport.dispatcher.database.dao.ContractorRepository;
import ru.sber.transport.dispatcher.database.dao.DispatcherRepository;
import ru.sber.transport.dispatcher.database.dao.VehicleRepository;
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
@DisplayName("Проверка импорта/экспорта автомобилей")
@MockitoBean(types = Key.class)
@MockitoBean(types = JwtDecoder.class)
class VehicleImportExportTest extends KafkaTest {

    private static final String DISPATCHER_ROOM_ADMIN_ROLE = "ROLE_DISPATCHER_ROOM_ADMIN";

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
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UploadStates uploadStates;

    @MockitoBean
    private AuthorizationManager<?> manager;

    private Contractor contractor;
    private Autopark autopark;
    private Autopark autopark2;
    private Dispatcher dispatcher;

    @BeforeEach
    void createRepository() {
        contractor = contractorRepository.save(TestContractors.createTestContractor());
        autopark = autoparkRepository.save(TestAutoparks.createTestAutopark(contractor));
        Autopark testAutopark = TestAutoparks.createTestAutopark(contractor);
        testAutopark.setName(UUID.randomUUID().toString());
        autopark2 = autoparkRepository.save(testAutopark);

        var dispatcher = new Dispatcher();

        dispatcher.setLastName("Фамилия");
        dispatcher.setFirstName("Имя");
        dispatcher.setPatronymic("Отчество");
        dispatcher.setHumanReadableId("Старое HRI");
        dispatcher.setEmail("your@email.com");
        dispatcher.setPhone("+79000000000");
        dispatcher.setContractor(contractor);

        this.dispatcher = dispatcherRepository.save(dispatcher);

        AuthorizeUtils.authorize(manager, "ROLE_USER");
    }

    @AfterEach
    void clearRepository() {
        vehicleRepository.deleteAllInBatch();
        dispatcherRepository.deleteAllInBatch();
        autoparkRepository.deleteAllInBatch();
        contractorRepository.deleteAllInBatch();
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка экспорта автомобилей")
    void testExportData() {
        var itemCount = 10;
        for (var i = 0; i < itemCount; i++) {
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestPassengerVehicle(autopark, i), i));
        }

        for (var i = 0; i < itemCount; i++) {
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestCargoVehicle(autopark, i), i));
        }

        for (var i = 0; i < itemCount; i++) {
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestUniversalVehicle(autopark, i), i));
        }

        var content = mockMvc.perform(get("/files/vehicles?autoparkId=" + autopark.getId().toString())
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

        var expectedList = vehicleRepository.findAll();

        expectedList.sort(Comparator.comparing(Vehicle::getStateNumber));

        try (var byteArrayInputStream = new ByteArrayInputStream(bytes); var excel = new XSSFWorkbook(byteArrayInputStream)) {
            checkCargoExport(expectedList, excel);
            checkPassengerExport(expectedList, excel);
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка экспорта пустого шаблона")
    void testExportSample() {
        var itemCount = 10;
        for (var i = 0; i < itemCount; i++) {
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestPassengerVehicle(autopark, i), i));
        }

        for (var i = 0; i < itemCount; i++) {
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestCargoVehicle(autopark, i), i));
        }

        var content = mockMvc.perform(get("/files/vehicles?autoparkId=%s&isSample=true".formatted(autopark.getId().toString()))
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

        var expectedList = vehicleRepository.findAll();

        expectedList.sort(Comparator.comparing(Vehicle::getStateNumber));

        try (var byteArrayInputStream = new ByteArrayInputStream(bytes); var excel = new XSSFWorkbook(byteArrayInputStream)) {
            var sheet = excel.getSheet("Грузовые");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);

            sheet = excel.getSheet("Легковые");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка импорта автомобилей")
    void testImport() {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/vehicles.xlsx"));

        mockMvc.perform(multipart("/files/vehicles/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("vehicles", dispatcher.getId().toString()).size(), equalTo(1));

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
                .until(() -> uploadStates.getResults("vehicles", dispatcher.getId().toString())
                        .getFirst().getFinished(), equalTo(true));

        List<Vehicle> vehicles = vehicleRepository.findAll();

        Assertions.assertThat(vehicles)
                .hasSize(20);

        Assertions.assertThat(vehicles.stream().filter(v -> v.getVehicleType() == CARGO).count())
                .isEqualTo(10);

        Assertions.assertThat(vehicles.stream().filter(v -> v.getVehicleType() == PASSENGER).count())
                .isEqualTo(10);
    }

    @Test
    @DisplayName("Фильтр транспортных средств")
    void testExportWithFilters() throws Exception {
        var itemCount = 10;
        for (var i = 0; i < itemCount; i++) {
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestPassengerVehicle(autopark, i), i));
        }

        for (var i = 0; i < itemCount; i++) {
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestCargoVehicle(autopark, i), i));
        }

        Vehicle vehicle = vehicleRepository.findAll().getFirst();
        vehicle.setAutopark(autopark2);
        vehicle.setStateNumber("X102XX163RUS");
        vehicle.setPassport("00TK000003");
        vehicle.setVin("XYX000000X0001002");
        vehicle.setManufactureYear(2002);
        vehicle.setTransmissionType("Mechanical");
        vehicle.getModel().setBrand("Bentley");
        vehicle.getModel().setName("Motors");
        vehicle.setVehicleType(CARGO);
        vehicleRepository.save(vehicle);

        var urlTemplate = "/files/vehicles?autoparkId=%s&&manufactureYear=%d&transmissionType=%s&brand=%s&model=%s&vehicleType=%s"
                .formatted(autopark2.getId(), 2002, "Mechanical", "Bentley", "Motors", vehicle.getVehicleType());

        var content = mockMvc.perform(get(urlTemplate)
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
            var sheet = excel.getSheet("Грузовые");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2);

            sheet = excel.getSheet("Легковые");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Экспорт с ошибкой в автопарке")
    void testExportErrorWrongAutopark() throws Exception {
        var itemCount = 10;
        for (var i = 0; i < itemCount; i++) {
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestPassengerVehicle(autopark, i), i));
        }

        for (var i = 0; i < itemCount; i++) {
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestCargoVehicle(autopark, i), i));
        }

        var urlTemplate = "/files/vehicles?autoparkId=%s"
                .formatted(UUID.randomUUID());

        var content = mockMvc.perform(get(urlTemplate)
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
            var sheet = excel.getSheet("Грузовые");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);

            sheet = excel.getSheet("Легковые");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Экспорт с пустым автопарком")
    void testExportErrorEmptyAutopark() throws Exception {
        var itemCount = 10;
        for (var i = 0; i < itemCount; i++) {
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestPassengerVehicle(autopark, i), i));
        }

        for (var i = 0; i < itemCount; i++) {
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestCargoVehicle(autopark, i), i));
        }

        var content = mockMvc.perform(get("/files/vehicles")
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

        var expectedList = vehicleRepository.findAll();

        expectedList.sort(Comparator.comparing(Vehicle::getStateNumber));

        try (var byteArrayInputStream = new ByteArrayInputStream(bytes); var excel = new XSSFWorkbook(byteArrayInputStream)) {
            checkCargoExport(expectedList, excel);
            checkPassengerExport(expectedList, excel);
        }
    }

    @Test
    @DisplayName("Админ может экспортировать данные по указанному contractorId")
    void testExportByAdminWithContractorId() throws Exception {
        var otherContractor = contractorRepository.save(TestContractors.createTestContractor());
        var otherAutopark = autoparkRepository.save(TestAutoparks.createTestAutopark(otherContractor));

        var vehicle = TestVehicles.createTestPassengerVehicle(otherAutopark, 0);
        vehicleRepository.save(vehicle);

        var content = mockMvc.perform(get("/files/vehicles?autoparkId=" + otherAutopark.getId() +
                        "&contractorId=" + otherContractor.getId())
                .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                .claim("roles", List.of(DISPATCHER_ROOM_ADMIN_ROLE, "ROLE_USER")))
                        .authorities(new SimpleGrantedAuthority(DISPATCHER_ROOM_ADMIN_ROLE),
                                new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        var response = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {});

        var result = await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(10))
                .until(() -> mockMvc.perform(get(response.get("result_url") + "/")
                                .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                        .claim("roles",  List.of(DISPATCHER_ROOM_ADMIN_ROLE, "ROLE_USER")))
                                        .authorities(new SimpleGrantedAuthority(DISPATCHER_ROOM_ADMIN_ROLE),
                                                new SimpleGrantedAuthority("ROLE_USER"))))
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
        try (var inputStream = new ByteArrayInputStream(bytes); var workbook = new XSSFWorkbook(inputStream)) {
            var sheet = workbook.getSheet("Легковые");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2); // header + 1 vehicle
            assertThat(sheet.getRow(1).getCell(2).getStringCellValue()).isEqualTo(vehicle.getStateNumber());
        }
    }

    @Test
    @DisplayName("Федеральный диспетчер может экспортировать данные по указанному contractorId")
    void testExportByFederalDispatcherWithContractorId() throws Exception {
        var otherContractor = contractorRepository.save(TestContractors.createTestContractor());
        var otherAutopark = autoparkRepository.save(TestAutoparks.createTestAutopark(otherContractor));

        var vehicle = TestVehicles.createTestPassengerVehicle(otherAutopark, 0);
        vehicleRepository.save(vehicle);

        var content = mockMvc.perform(get("/files/vehicles?autoparkId=" + otherAutopark.getId() +
                        "&contractorId=" + otherContractor.getId())
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                        .claim("roles", List.of(ROLE_FEDERAL_DISPATCHER_CONTRACTOR, "ROLE_USER")))
                                .authorities(new SimpleGrantedAuthority(ROLE_FEDERAL_DISPATCHER_CONTRACTOR),
                                        new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        var response = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {});

        var result = await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(10))
                .until(() -> mockMvc.perform(get(response.get("result_url") + "/")
                                .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())
                                                .claim("roles",  List.of(ROLE_FEDERAL_DISPATCHER_CONTRACTOR, "ROLE_USER")))
                                        .authorities(new SimpleGrantedAuthority(ROLE_FEDERAL_DISPATCHER_CONTRACTOR),
                                                new SimpleGrantedAuthority("ROLE_USER"))))
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
        try (var inputStream = new ByteArrayInputStream(bytes); var workbook = new XSSFWorkbook(inputStream)) {
            var sheet = workbook.getSheet("Легковые");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2); // header + 1 vehicle
            assertThat(sheet.getRow(1).getCell(2).getStringCellValue()).isEqualTo(vehicle.getStateNumber());
        }
    }

    private void checkCargoExport(List<Vehicle> expectedList, XSSFWorkbook excel) {
        var sheet = excel.getSheet("Грузовые");

        var row = sheet.getRow(0);
        assertThat(row.getCell(0).getStringCellValue()).isEqualTo("Тип");
        assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Принадлежность к автопарку");
        assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Регистрационный знак");
        assertThat(row.getCell(3).getStringCellValue()).isEqualTo("Марка ТС");
        assertThat(row.getCell(4).getStringCellValue()).isEqualTo("Автомобиль находится в эксплуатации");
        assertThat(row.getCell(5).getStringCellValue()).isEqualTo("Модель ТС");
        assertThat(row.getCell(6).getStringCellValue()).isEqualTo("Цвет");
        assertThat(row.getCell(7).getStringCellValue()).isEqualTo("Идентификационный номер (VIN)");
        assertThat(row.getCell(8).getStringCellValue()).isEqualTo("Разрешенная максимальная масса, т.");
        assertThat(row.getCell(9).getStringCellValue()).isEqualTo("Объем, м3");
        assertThat(row.getCell(10).getStringCellValue()).isEqualTo("Длина, м");
        assertThat(row.getCell(11).getStringCellValue()).isEqualTo("Ширина, м");
        assertThat(row.getCell(12).getStringCellValue()).isEqualTo("Высота, м");

        var cargoVehicles = expectedList
                .stream()
                .filter(vehicle -> vehicle.getVehicleType() == CARGO)
                .toList();

        assertThat(sheet.getPhysicalNumberOfRows())
                .isEqualTo(cargoVehicles.size() + 1);

        for (int rowIndex = 1, index = 0; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++, index++) {
            var actualRow = sheet.getRow(rowIndex);
            var expected = cargoVehicles.get(index);

            var autoPark = autoparkRepository.findById(expected.getAutopark().getId())
                    .orElseThrow();

            assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo("Грузовой");
            assertThat(actualRow.getCell(1).getStringCellValue()).isEqualTo(autoPark.getName());
            assertThat(actualRow.getCell(2).getStringCellValue()).isEqualTo(expected.getStateNumber());
            assertThat(actualRow.getCell(3).getStringCellValue()).isEqualTo(expected.getModel().getBrand());
            assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo(expected.isInExploitation() ? "Да" : "Нет");
            assertThat(actualRow.getCell(5).getStringCellValue()).isEqualTo(expected.getModel().getName());
            assertThat(actualRow.getCell(6).getStringCellValue()).isEqualTo(expected.getColor());
            assertThat(actualRow.getCell(7).getStringCellValue()).isEqualTo(expected.getVin());
            assertThat(actualRow.getCell(8).getNumericCellValue()).isEqualTo(Double.valueOf(expected.getMaxAllowedWeight()));
            assertThat(expected.getVehicleAdditional())
                    .isNotNull();

            var cargoAdditional = objectMapper.convertValue(expected.getVehicleAdditional(), CargoVehicleData.class);
            assertThat(actualRow.getCell(9).getNumericCellValue()).isEqualTo(cargoAdditional.getVolume());
            assertThat(actualRow.getCell(10).getNumericCellValue()).isEqualTo(cargoAdditional.getLength());
            assertThat(actualRow.getCell(11).getNumericCellValue()).isEqualTo(cargoAdditional.getWidth());
            assertThat(actualRow.getCell(12).getNumericCellValue()).isEqualTo(cargoAdditional.getHeight());
        }
    }

    private void checkPassengerExport(List<Vehicle> expectedList, XSSFWorkbook excel) {
        var sheet = excel.getSheet("Легковые");

        var row = sheet.getRow(0);
        assertThat(row.getCell(0).getStringCellValue()).isEqualTo("Тип");
        assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Принадлежность к автопарку");
        assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Регистрационный знак");
        assertThat(row.getCell(3).getStringCellValue()).isEqualTo("Марка ТС");
        assertThat(row.getCell(4).getStringCellValue()).isEqualTo("Автомобиль находится в эксплуатации");
        assertThat(row.getCell(5).getStringCellValue()).isEqualTo("Модель ТС");
        assertThat(row.getCell(6).getStringCellValue()).isEqualTo("Цвет");
        assertThat(row.getCell(7).getStringCellValue()).isEqualTo("Идентификационный номер (VIN)");
        assertThat(row.getCell(8).getStringCellValue()).isEqualTo("Разрешенная максимальная масса, т.");
        assertThat(row.getCell(9).getStringCellValue()).isEqualTo("Серия, номер полиса обязательного страхования ТС");
        assertThat(row.getCell(10).getStringCellValue()).isEqualTo("Экологический класс");
        assertThat(row.getCell(11).getStringCellValue()).isEqualTo("Расход топлива, л/км");
        assertThat(row.getCell(12).getStringCellValue()).isEqualTo("Комплектация");
        assertThat(row.getCell(13).getStringCellValue()).isEqualTo("Пробег, км");
        assertThat(row.getCell(14).getStringCellValue()).isEqualTo("Год выпуска");
        assertThat(row.getCell(15).getStringCellValue()).isEqualTo("Тип привода");
        assertThat(row.getCell(16).getStringCellValue()).isEqualTo("Тип двигателя");
        assertThat(row.getCell(17).getStringCellValue()).isEqualTo("Тип трансмиссии");
        assertThat(row.getCell(18).getStringCellValue()).isEqualTo("Тип кузова");
        assertThat(row.getCell(19).getStringCellValue()).isEqualTo("Паспорт ТС");

        var passengerVehicles = expectedList
                .stream()
                .filter(vehicle -> vehicle.getVehicleType() == VehicleType.PASSENGER)
                .toList();

        assertThat(sheet.getPhysicalNumberOfRows())
                .isEqualTo(passengerVehicles.size() + 1);

        for (int rowIndex = 1, index = 0; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++, index++) {
            var actualRow = sheet.getRow(rowIndex);
            var expected = passengerVehicles.get(index);

            var autoPark = autoparkRepository.findById(expected.getAutopark().getId())
                    .orElseThrow();

            assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo("Легковой");
            assertThat(actualRow.getCell(1).getStringCellValue()).isEqualTo(autoPark.getName());
            assertThat(actualRow.getCell(2).getStringCellValue()).isEqualTo(expected.getStateNumber());
            assertThat(actualRow.getCell(3).getStringCellValue()).isEqualTo(expected.getModel().getBrand());
            assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo(expected.isInExploitation() ? "Да" : "Нет");
            assertThat(actualRow.getCell(5).getStringCellValue()).isEqualTo(expected.getModel().getName());
            assertThat(actualRow.getCell(6).getStringCellValue()).isEqualTo(expected.getColor());
            assertThat(actualRow.getCell(7).getStringCellValue()).isEqualTo(expected.getVin());
            assertThat(actualRow.getCell(8).getNumericCellValue()).isEqualTo(Double.valueOf(expected.getMaxAllowedWeight()));
            assertThat(actualRow.getCell(9).getStringCellValue()).isEqualTo(expected.getInsuranceNumber());
            assertThat(actualRow.getCell(10).getStringCellValue()).isEqualTo(expected.getEcoClass().getRusName());
            assertThat(actualRow.getCell(11).getNumericCellValue()).isEqualTo(expected.getFuelConsumption());
            assertThat(actualRow.getCell(12).getStringCellValue()).isEqualTo(expected.getPackageClass());
            assertThat(actualRow.getCell(13).getNumericCellValue()).isEqualTo(Double.valueOf(expected.getMileage()));
            assertThat(actualRow.getCell(14).getNumericCellValue()).isEqualTo(Double.valueOf(expected.getModel().getYear()));
            assertThat(actualRow.getCell(15).getStringCellValue()).isEqualTo(expected.getChassisType());
            assertThat(actualRow.getCell(16).getStringCellValue()).isEqualTo(expected.getEngineType());
            assertThat(actualRow.getCell(17).getStringCellValue()).isEqualTo(expected.getTransmissionType());
            assertThat(actualRow.getCell(18).getStringCellValue()).isEqualTo(expected.getBodyType());
            assertThat(actualRow.getCell(19).getStringCellValue()).isEqualTo(expected.getPassport());

        }
    }
}

