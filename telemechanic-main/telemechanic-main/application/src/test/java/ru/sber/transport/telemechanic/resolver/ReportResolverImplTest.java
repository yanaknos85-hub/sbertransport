package ru.sber.transport.telemechanic.resolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.file_works.database.dao.ExportTaskRepository;
import ru.sber.transport.file_works.database.migrations.DatabaseMigration;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.*;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.RegistryExcelDto;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.FileStatus;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.enumerate.TransportStatus;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.telemechanic.TestData.*;
import static ru.sber.transport.telemechanic.enumerate.CheckType.INSTRUMENT_PANEL;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_ADMIN_CORP_CLIENT;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_ADMIN_DATA_MASTER;

@DisplayName("Экспорт/импорт реестра")
@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
@TestPropertySource(properties = "export.tempDir=" + ReportResolverImplTest.TEMP_DIR)
class ReportResolverImplTest {

    public static final String FILES_REGISTRY_URI = "/files/registry/";

    public static final String RESULT_URL_URI = "result_url";

    public static final String TEMP_DIR = "target/test/files";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DatabaseMigration databaseMigration;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private TransportRepository transportRepository;

    @Autowired
    private CheckPhotoRepository checkPhotoRepository;

    @Autowired
    private CheckRepository checkRepository;

    @Autowired
    private ExportTaskRepository exportTaskRepository;

    @MockitoBean
    private AuthorizationManager<?> manager;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(manager);
        var organization1 = organizationRepository.save(createOrganization1());
        var department1 = departmentRepository.save(createDepartment1(organization1, null));
        var position1 = positionRepository.save(createPosition1(organization1));
        var employee1 = employeeRepository.save(createEmployee1(department1, position1));
        var employee2 = employeeRepository.save(createEmployee2(department1, position1));
        var requests = new ArrayList<Request>();
        var count = 15;
        for (var i = 0; i < count; i++) {
            var request = createRequest(i % 2 == 0 ? employee1 : employee2, null, ORGANIZATION_1_ID);
            var transport = transportRepository.save(
                new Transport(UUID.randomUUID(),
                    "А"
                        .concat(RandomStringUtils.random(3, false, true))
                        .concat("ЕК")
                        .concat(RandomStringUtils.random(3, false, true)),
                    "LADA",
                    "VESTA",
                    100000,
                    TransportStatus.IN_USE,
                    "-",
                    "-",
                    40,
                    new HashSet<>(Set.of(ORGANIZATION_1)), null, null, null));
            request.setTransport(transport);
            request.setHumanReadableId(String.format("HRU-%03d", i));

            switch (i % 10) {
                case 0 -> {
                    request.setStatus(RequestStatus.WARNING);
                    request.setChecksStartedTime(request.getCreationTime());
                    request.setChecksFinishedTime(request.getCreationTime().plusMinutes(30));
                    updateChecksForWarningStatus(request);
                }
                case 1 -> {
                    request.setStatus(RequestStatus.DONE);
                    request.setChecksStartedTime(request.getCreationTime());
                    request.setChecksFinishedTime(request.getCreationTime().plusMinutes(30));
                    updateChecksForDoneStatus(request);
                }
                case 2 -> {
                    request.setStatus(RequestStatus.CANCELED);
                    updateChecksForWarningStatus(request);
                }
                case 3 -> {
                    request.setStatus(RequestStatus.EXPIRED);
                    updateChecksForDoneStatus(request);
                }
                case 4 -> {
                    request.setStatus(RequestStatus.FINISHED);
                    request.setChecksStartedTime(request.getCreationTime());
                    request.setInspectionTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(9L));
                    request.setInspector(employee2);
                    updateChecksForDoneStatus(request);
                }
                case 5 -> {
                    request.setStatus(RequestStatus.ON_THE_LINE);
                    request.setChecksStartedTime(request.getCreationTime());
                    request.setInspectionTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(12L));
                    request.setInspector(employee2);
                    request.setCreationTime(request.getCreationTime().minusYears(5));
                    request.setComment("Все хорошо");
                    updateChecksForDoneStatus(request);
                }
                case 6 -> {
                    request.setStatus(RequestStatus.DECLINED);
                    request.setInspectionTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(12L));
                    request.setInspector(employee2);
                    request.setCreationTime(request.getCreationTime().minusYears(5));
                    request.setComment("Все плохо");
                    updateChecksForWarningStatus(request);
                }
                default -> {
                }
            }
            requests.add(request);
        }
        requestRepository.saveAll(requests);
    }

    @AfterEach
    void deleteTestFolder() throws IOException {
        FileUtils.deleteDirectory(new File(TEMP_DIR + "/files"));
        checkPhotoRepository.deleteAll();
        checkRepository.deleteAll();
        requestRepository.deleteAll();
        transportRepository.deleteAll();
        employeeRepository.deleteAll();
        positionRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    @DisplayName("Экспорт")
    void export() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_DATA_MASTER.name());
        var count = 15;
        var content = mockMvc.perform(get(FILES_REGISTRY_URI)
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name()))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString();
        var response = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {
        });
        var file = response.get(RESULT_URL_URI);
        await().timeout(Duration.ofSeconds(15)).pollDelay(Duration.ofSeconds(3))
            .until(() -> {
                var data = exportTaskRepository.findByName(file);
                if (data != null) {
                    return data.getDone();
                }
                return false;
            });
        var bytes = mockMvc.perform(get(response.get(RESULT_URL_URI))
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name()))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet("Журнал выпуска ТС на линию");
            assertThat(sheet).isNotNull();
            var row0 = sheet.getRow(0);
            assertThat(row0).isNotNull();
            assertThat(row0.getPhysicalNumberOfCells()).isEqualTo(1);
            assertThat(row0.getCell(0).getStringCellValue()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
            checkHeaderRow(sheet.getRow(1));
            var expectedList = requestRepository.findAll(Sort.by(Request_.HUMAN_READABLE_ID));
            for (int rowIndex = 2, index = 0; index < count; rowIndex++, index++) {
                checkRow(rowIndex, index, sheet.getRow(rowIndex), expectedList.get(index), OFFICIAL_NAME_1);
            }
        }
    }

    @Test
    @DisplayName("Экспорт без роли, дающей доступ ко всем организациям")
    void exportNotHaveAllOrganizationsRole() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var organization2 = organizationRepository.save(createOrganization2());
        var department2 = departmentRepository.save(createDepartment2(organization2, null));
        var position2 = positionRepository.save(createPosition2(organization2));
        var employee3 = employeeRepository.save(createEmployee3(department2, position2));
        var requests = new ArrayList<Request>();
        var count = 5;
        for (var i = 0; i < count; i++) {
            var request = createRequest(employee3, null, ORGANIZATION_2_ID);
            var transport = transportRepository.save(
                new Transport(UUID.randomUUID(),
                    "А"
                        .concat(RandomStringUtils.random(3, false, true))
                        .concat("ЕК")
                        .concat(RandomStringUtils.random(3, false, true)),
                    "LADA",
                    "VESTA",
                    100000,
                    TransportStatus.IN_USE,
                    "-",
                    "-",
                    40,
                    new HashSet<>(Set.of(ORGANIZATION_1)), null, null, null));
            request.setTransport(transport);
            request.setHumanReadableId(String.format("HRU-%03d", i));
            requests.add(request);
        }
        requestRepository.saveAll(requests);
        requestRepository.flush();
        var content = mockMvc.perform(get(FILES_REGISTRY_URI)
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_3_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString();
        var response = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {
        });
        var file = response.get(RESULT_URL_URI);
        await().timeout(Duration.ofSeconds(15)).pollDelay(Duration.ofSeconds(3))
            .until(() -> {
                var data = exportTaskRepository.findByName(file);
                if (data != null) {
                    return data.getDone();
                }
                return false;
            });
        var bytes = mockMvc.perform(get(response.get(RESULT_URL_URI))
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet("Журнал выпуска ТС на линию");
            assertThat(sheet).isNotNull();
            var row0 = sheet.getRow(0);
            assertThat(row0).isNotNull();
            assertThat(row0.getPhysicalNumberOfCells()).isEqualTo(1);
            assertThat(row0.getCell(0).getStringCellValue()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
            checkHeaderRow(sheet.getRow(1));
            var expectedList = requestRepository.findAll(Sort.by(Request_.HUMAN_READABLE_ID))
                .stream()
                .filter(request -> request.getAuthor().equals(employee3))
                .toList();
            for (int rowIndex = 2, index = 0; index < count; rowIndex++, index++) {
                checkRow(rowIndex, index, sheet.getRow(rowIndex), expectedList.get(index), OFFICIAL_NAME_2);
            }
        }
    }

    @Test
    @DisplayName("Экспорт реестра с фильтром humanReadableId")
    void exportWithHumanReadableIdFilter() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_DATA_MASTER.name());
        var content = mockMvc.perform(get(FILES_REGISTRY_URI)
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                .param("humanReadableId", "HRU-001"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString();
        var response = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {
        });
        var file = response.get(RESULT_URL_URI);
        await().timeout(Duration.ofSeconds(15)).pollDelay(Duration.ofSeconds(3))
            .until(() -> {
                var data = exportTaskRepository.findByName(file);
                if (data != null) {
                    return data.getDone();
                }
                return false;
            });
        var bytes = mockMvc.perform(get(response.get(RESULT_URL_URI))
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name()))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
        try (var bais = new ByteArrayInputStream(bytes)) {
            try (var workbook = new XSSFWorkbook(bais)) {
                var sheet = workbook.getSheet("Журнал выпуска ТС на линию");
                assertThat(sheet).isNotNull();
                var row0 = sheet.getRow(0);
                assertThat(row0).isNotNull();
                assertThat(row0.getPhysicalNumberOfCells()).isEqualTo(1);
                assertThat(row0.getCell(0).getStringCellValue()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
                checkHeaderRow(sheet.getRow(1));
                var expectedList = requestRepository.findAllRegistryByOrganizationId(null, "HRU-001", null,
                    null, null, null,
                    false);
                expectedList = enhanceWithDepartmentChain(expectedList);
                for (int rowIndex = 2, index = 0; index < expectedList.size(); rowIndex++, index++) {
                    checkExcelRow(rowIndex, index, sheet.getRow(rowIndex), expectedList.get(index));
                }
            }
        }
    }

    @Test
    @DisplayName("Экспорт реестра с фильтром personnelNumber")
    void exportWithPersonnelNumberFilter() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_DATA_MASTER.name());
        var content = mockMvc.perform(get(FILES_REGISTRY_URI)
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                .param("personnelNumber", "0000001"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString();
        var response = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {
        });
        var file = response.get(RESULT_URL_URI);
        await().timeout(Duration.ofSeconds(15)).pollDelay(Duration.ofSeconds(3))
            .until(() -> {
                var data = exportTaskRepository.findByName(file);
                if (data != null) {
                    return data.getDone();
                }
                return false;
            });
        var bytes = mockMvc.perform(get(response.get(RESULT_URL_URI))
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name()))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
        try (var bais = new ByteArrayInputStream(bytes)) {
            try (var workbook = new XSSFWorkbook(bais)) {
                var sheet = workbook.getSheet("Журнал выпуска ТС на линию");
                assertThat(sheet).isNotNull();
                var row0 = sheet.getRow(0);
                assertThat(row0).isNotNull();
                assertThat(row0.getPhysicalNumberOfCells()).isEqualTo(1);
                assertThat(row0.getCell(0).getStringCellValue()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
                checkHeaderRow(sheet.getRow(1));
                var expectedList = requestRepository.findAllRegistryByOrganizationId(null, null, "0000001",
                    null, null, null,
                    false);
                expectedList = enhanceWithDepartmentChain(expectedList);
                for (int rowIndex = 2, index = 0; index < expectedList.size(); rowIndex++, index++) {
                    checkExcelRow(rowIndex, index, sheet.getRow(rowIndex), expectedList.get(index));
                }
            }
        }
    }

    @Test
    @DisplayName("Экспорт реестра с фильтром startCreationDate и endCreationDate")
    void exportWithStartCreationDateAndEndCreationDateFilter() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_DATA_MASTER.name());
        var content = mockMvc.perform(get(FILES_REGISTRY_URI)
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                .param("startCreationTime", LocalDateTime.now().minusYears(1).toString())
                .param("endCreationTime", LocalDateTime.now().plusYears(1).toString()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString();
        var response = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {
        });
        var file = response.get(RESULT_URL_URI);
        await().timeout(Duration.ofSeconds(15)).pollDelay(Duration.ofSeconds(3))
            .until(() -> {
                var data = exportTaskRepository.findByName(file);
                if (data != null) {
                    return data.getDone();
                }
                return false;
            });
        var bytes = mockMvc.perform(get(response.get(RESULT_URL_URI))
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name()))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
        try (var bais = new ByteArrayInputStream(bytes)) {
            try (var workbook = new XSSFWorkbook(bais)) {
                var sheet = workbook.getSheet("Журнал выпуска ТС на линию");
                assertThat(sheet).isNotNull();
                var row0 = sheet.getRow(0);
                assertThat(row0).isNotNull();
                assertThat(row0.getPhysicalNumberOfCells()).isEqualTo(1);
                assertThat(row0.getCell(0).getStringCellValue()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
                checkHeaderRow(sheet.getRow(1));
                var expectedList = requestRepository.findAllRegistryByOrganizationId(null, null, null,
                    LocalDateTime.now().minusYears(1),
                    LocalDateTime.now().plusYears(1),
                    null, false);
                expectedList = enhanceWithDepartmentChain(expectedList);
                for (int rowIndex = 2, index = 0; index < expectedList.size(); rowIndex++, index++) {
                    checkExcelRow(rowIndex, index, sheet.getRow(rowIndex), expectedList.get(index));
                }
            }
        }
    }

    @Test
    @DisplayName("Экспорт реестра со всеми фильтрами")
    void exportWithAllFilters() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_DATA_MASTER.name());
        var content = mockMvc.perform(get(FILES_REGISTRY_URI)
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                .param("humanReadableId", "HRU-015")
                .param("personnelNumber", "0000002")
                .param("startCreationTime", LocalDateTime.now().minusYears(1).toString())
                .param("endCreationTime", LocalDateTime.now().plusYears(1).toString()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString();
        var response = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {
        });
        var file = response.get(RESULT_URL_URI);
        await().timeout(Duration.ofSeconds(15)).pollDelay(Duration.ofSeconds(3))
            .until(() -> {
                var data = exportTaskRepository.findByName(file);
                if (data != null) {
                    return data.getDone();
                }
                return false;
            });
        var bytes = mockMvc.perform(get(response.get(RESULT_URL_URI))
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name()))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
        try (var bais = new ByteArrayInputStream(bytes)) {
            try (var workbook = new XSSFWorkbook(bais)) {
                var sheet = workbook.getSheet("Журнал выпуска ТС на линию");
                assertThat(sheet).isNotNull();
                var row0 = sheet.getRow(0);
                assertThat(row0).isNotNull();
                assertThat(row0.getPhysicalNumberOfCells()).isEqualTo(1);
                assertThat(row0.getCell(0).getStringCellValue()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
                checkHeaderRow(sheet.getRow(1));
                var expectedList = requestRepository.findAllRegistryByOrganizationId(null, "HRU-015", "0000002",
                    LocalDateTime.now().minusYears(1),
                    LocalDateTime.now().plusYears(1),
                    null, false);
                expectedList = enhanceWithDepartmentChain(expectedList);
                for (int rowIndex = 2, index = 0; index < expectedList.size(); rowIndex++, index++) {
                    checkExcelRow(rowIndex, index, sheet.getRow(rowIndex), expectedList.get(index));
                }
            }
        }
    }

    @Test
    @DisplayName("Получение реестра с фильтром идентификатор департамента и идентификатор организации")
    void shouldReturnRegistryWithFilterDepartmentIdsAndOrganizationId() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_DATA_MASTER.name());
        var content = mockMvc.perform(get(FILES_REGISTRY_URI)
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                .param("organizationId", ORGANIZATION_1_ID.toString())
                .param("departmentIds", DEPARTMENT_1_ID.toString().concat(",").concat(DEPARTMENT_2_ID.toString())))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString();
        var response = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {
        });
        var file = response.get(RESULT_URL_URI);
        await().timeout(Duration.ofSeconds(15)).pollDelay(Duration.ofSeconds(3))
            .until(() -> {
                var data = exportTaskRepository.findByName(file);
                if (data != null) {
                    return data.getDone();
                }
                return false;
            });
        var bytes = mockMvc.perform(get(response.get(RESULT_URL_URI))
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name()))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
        try (var bais = new ByteArrayInputStream(bytes)) {
            try (var workbook = new XSSFWorkbook(bais)) {
                var sheet = workbook.getSheet("Журнал выпуска ТС на линию");
                assertThat(sheet).isNotNull();
                var row0 = sheet.getRow(0);
                assertThat(row0).isNotNull();
                assertThat(row0.getPhysicalNumberOfCells()).isEqualTo(1);
                assertThat(row0.getCell(0).getStringCellValue()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
                checkHeaderRow(sheet.getRow(1));
                var expectedList = requestRepository.findAllRegistryByOrganizationId(ORGANIZATION_1_ID, null, null,
                    null, null,
                    Set.of(DEPARTMENT_1_ID, DEPARTMENT_2_ID), true);
                expectedList = enhanceWithDepartmentChain(expectedList);
                for (int rowIndex = 2, index = 0; index < expectedList.size(); rowIndex++, index++) {
                    checkExcelRow(rowIndex, index, sheet.getRow(rowIndex), expectedList.get(index));
                }
            }
        }
    }

    private static void checkRow(int rowIndex, int index, XSSFRow actualRow, Request expected,
        String organizationName) {
        assertAll("Checking row " + rowIndex + " equals with element " + index,
            () -> assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(expected.getHumanReadableId()),
            () -> assertThat(actualRow.getCell(1).getStringCellValue()).isEqualTo(organizationName),
            () -> checkLocalDateTimeCell(actualRow, 2, expected.getCreationTime()),
            () -> checkLocalDateTimeCell(actualRow, 3, expected.getChecksStartedTime()),
            () -> checkLocalDateTimeCell(actualRow, 4, expected.getChecksFinishedTime()),
            () -> checkLocalDateTimeCell(actualRow, 5, expected.getInspectionTime()),
            () -> checkInspectionMark(actualRow, expected.getStatus()),
            () -> assertThat(actualRow.getCell(7).getStringCellValue()).isEqualTo(expected.getTransport().getStateNumber()),
            () -> assertThat(actualRow.getCell(8).getStringCellValue()).isEqualTo(expected.getTransport().getBrand()),
            () -> assertThat(actualRow.getCell(9).getStringCellValue()).isEqualTo(expected.getTransport().getModel()),
            () -> assertThat(actualRow.getCell(10).getStringCellValue()).isEqualTo(expected.getAuthor().getPersonnelNumber()),
            () -> assertThat(actualRow.getCell(11).getStringCellValue()).isEqualTo(expected.getAuthor().getFIO()),
            () -> checkStringCell(actualRow, 12,
                Objects.isNull(expected.getInspector()) ? "" : expected.getInspector().getPersonnelNumber()),
            () -> checkStringCell(actualRow, 13,
                Objects.isNull(expected.getInspector()) ? "" : expected.getInspector().getFIO()),
            () -> checkStringCell(actualRow, 14, expected.getComment())
        );
    }

    private static void checkExcelRow(int rowIndex, int index, XSSFRow actualRow, RegistryExcelDto expected) {
        assertAll("Checking row " + rowIndex + " equals with element " + index,
            () -> assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(expected.getHumanReadableId()),
            () -> assertThat(actualRow.getCell(1).getStringCellValue()).isEqualTo(OFFICIAL_NAME_1),
            () -> checkLocalDateTimeCell(actualRow, 2, expected.getCreationTime()),
            () -> checkLocalDateTimeCell(actualRow, 3, expected.getChecksStartedTime()),
            () -> checkLocalDateTimeCell(actualRow, 4, expected.getChecksFinishedTime()),
            () -> checkLocalDateTimeCell(actualRow, 5, expected.getInspectionTime()),
            () -> assertThat(actualRow.getCell(6).getStringCellValue()).isEqualTo(expected.getInspectionMark()),
            () -> assertThat(actualRow.getCell(7).getStringCellValue()).isEqualTo(expected.getStateNumber()),
            () -> assertThat(actualRow.getCell(8).getStringCellValue()).isEqualTo(expected.getBrand()),
            () -> assertThat(actualRow.getCell(9).getStringCellValue()).isEqualTo(expected.getModel()),
            () -> assertThat(actualRow.getCell(10).getStringCellValue()).isEqualTo(expected.getPersonnelNumber()),
            () -> assertThat(actualRow.getCell(11).getStringCellValue()).isEqualTo(expected.getFullName()),
            () -> checkStringCell(actualRow, 12, expected.getInspectorPersonnelNumber()),
            () -> checkStringCell(actualRow, 13, expected.getInspectorFullName()),
            () -> checkStringCell(actualRow, 14, expected.getComment()),
            () -> assertThat(actualRow.getCell(15).getStringCellValue()).isEqualTo(expected.getOrgStructureChain())
        );
    }

    private static void checkHeaderRow(XSSFRow row2) {
        assertAll("Checking header",
            () -> assertThat(row2.getCell(0).getStringCellValue()).isEqualTo("ID заявки"),
            () -> assertThat(row2.getCell(1).getStringCellValue()).isEqualTo("Организация"),
            () -> assertThat(row2.getCell(2).getStringCellValue()).isEqualTo("Дата и время создания заявки"),
            () -> assertThat(row2.getCell(3).getStringCellValue()).isEqualTo("Дата и время начала прохождения проверок"),
            () -> assertThat(row2.getCell(4).getStringCellValue()).isEqualTo("Дата и время завершения прохождения проверок"),
            () -> assertThat(row2.getCell(5).getStringCellValue()).isEqualTo("Дата и время проведения контроля"),
            () -> assertThat(row2.getCell(6).getStringCellValue()).isEqualTo("Отметка о прохождении контроля"),
            () -> assertThat(row2.getCell(7).getStringCellValue()).isEqualTo("Государственный номер"),
            () -> assertThat(row2.getCell(8).getStringCellValue()).isEqualTo("Марка"),
            () -> assertThat(row2.getCell(9).getStringCellValue()).isEqualTo("Модель"),
            () -> assertThat(row2.getCell(10).getStringCellValue()).isEqualTo("Табельный номер водителя"),
            () -> assertThat(row2.getCell(11).getStringCellValue()).isEqualTo("ФИО водителя"),
            () -> assertThat(row2.getCell(12).getStringCellValue()).isEqualTo("Табельный номер сотрудника, проводившего контроль"),
            () -> assertThat(row2.getCell(13).getStringCellValue()).isEqualTo("ФИО сотрудника, проводившего контроль"),
            () -> assertThat(row2.getCell(14).getStringCellValue()).isEqualTo("Комментарий"),
            () -> assertThat(row2.getCell(15).getStringCellValue()).isEqualTo("Подразделение")
        );
    }

    private static void checkStringCell(XSSFRow actualRow, int cellNumber, String expected) {
        var cellValue = actualRow.getCell(cellNumber).getStringCellValue();
        if (Objects.isNull(expected)) {
            assertThat(cellValue).isEmpty();
        } else {
            assertThat(cellValue).isEqualTo(expected);
        }
    }

    private static void checkLocalDateTimeCell(XSSFRow actualRow, int cellNumber, LocalDateTime expected) {
        var cellValue = actualRow.getCell(cellNumber).getLocalDateTimeCellValue();
        if (Objects.isNull(cellValue)) {
            assertThat(expected).isNull();
        } else {
            var expectedMoscowTime = OffsetDateTime.of(expected, ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of("Europe/Moscow"));
            assertThat(cellValue.truncatedTo(ChronoUnit.SECONDS)).isEqualTo(
                expectedMoscowTime.toLocalDateTime().truncatedTo(ChronoUnit.SECONDS));
        }
    }

    private static void checkInspectionMark(XSSFRow actualRow, RequestStatus status) {
        var cellValue = actualRow.getCell(6).getStringCellValue();
        if (status.equals(RequestStatus.ON_THE_LINE) || status.equals(RequestStatus.FINISHED)) {
            assertEquals("Пройден", cellValue);
        } else if (status.equals(RequestStatus.DECLINED)) {
            assertEquals("Не пройден", cellValue);
        } else {
            assertEquals("", cellValue);
        }
    }

    private void updateChecksForDoneStatus(Request request) {
        request.getChecks().forEach(check -> {
            check.setCheckStatus(CheckStatus.DONE);
            check.getPhotos().add(
                new CheckPhoto(null, check.getId(), LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                    FileStatus.UPLOADED));
        });
    }

    private void updateChecksForWarningStatus(Request request) {
        request.getChecks().forEach(check -> {
            check.setCheckStatus(CheckStatus.DONE);
            check.setAttempt(1);
            check.getPhotos().add(
                new CheckPhoto(null, check.getId(), LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                    FileStatus.UPLOADED));
            if (check.getCheckType().equals(INSTRUMENT_PANEL)) {
                check.setAttempt(2);
                check.setCheckStatus(CheckStatus.DECLINE);
                check.getPhotos().add(
                    new CheckPhoto(null, check.getId(), LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                        FileStatus.UPLOADED));
                check.getPhotos().add(
                    new CheckPhoto(null, check.getId(), LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                        FileStatus.UPLOADED));
            }
        });
    }

    private List<RegistryExcelDto> enhanceWithDepartmentChain(List<RegistryExcelDto> foundRequests) {
        var departmentIds = foundRequests.stream()
            .map(RegistryExcelDto::getDepartmentId)
            .collect(Collectors.toSet());
        var departmentChains = departmentRepository.findDepartmentChains(departmentIds).stream()
            .collect(Collectors.toMap(DepartmentWithChain::getId, DepartmentWithChain::getChain));

        return foundRequests.stream()
            .map(registryExcelDto -> {
                var chain = departmentChains.getOrDefault(registryExcelDto.getDepartmentId(), "");
                registryExcelDto.setOrgStructureChain(chain);
                return registryExcelDto;
            })
            .toList();
    }

}