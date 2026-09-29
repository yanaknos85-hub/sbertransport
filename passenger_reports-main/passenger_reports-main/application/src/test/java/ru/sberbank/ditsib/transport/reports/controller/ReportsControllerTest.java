package ru.sberbank.ditsib.transport.reports.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dao.*;
import ru.sberbank.ditsib.transport.reports.dto.PersonalUIVisibilityDTO;
import ru.sberbank.ditsib.transport.reports.dto.PublicUIVisibilityDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaskResultDto;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryCheckDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.mappers.EntityDTOMapper;
import ru.sberbank.ditsib.transport.reports.mappers.RequestMapper;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.reports.messaging.messages.UpdateTripRequestStatusMessage;
import ru.sberbank.ditsib.transport.reports.model.Address;
import ru.sberbank.ditsib.transport.reports.model.PaymentData;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.Waypoint;
import ru.sberbank.ditsib.transport.reports.service.DepartmentService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.transport.reports.service.FileService.FILE_NAME2;
import static ru.sberbank.ditsib.transport.reports.service.FileService.importExcelRegistry;
import static ru.sberbank.ditsib.transport.reports.service.TestRequestXlsExporter.*;

@Slf4j
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера отчетов")
@EmbeddedPostgres
@SpringBootTest
@MockitoBean(types = JwtDecoder.class)
class ReportsControllerTest extends SharedTest {

    public static final String TARGET_TEST_FILES = Path.of("target", "test", "files").toAbsolutePath().toString();
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private TripPurposeRepository tripPurposeRepository;

    @Autowired
    private WaypointRepository waypointRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RequestMapper requestMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SharedRideRepository sharedRideRepository;

    @Autowired
    private SingleTaxiTripRepository singleRepository;

    @Autowired
    private CoopTaxiTripRepository coopRepository;

    @Autowired
    private SharedRequestKpiRepository sharedRequestKpiRepository;

    @Autowired
    private OrderKpiRepository orderKpiRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private TaxiTariffRepository taxiTariffRepository;

    @Autowired
    private CarsharingTariffRepository carsharingTariffRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private EntityDTOMapper mapper;

    @Autowired
    private TaxiTripRegistryStringRepository ttrsRepository;

    @Autowired
    private TaxiTripRegistryRepository ttrRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private PersonalTariffRepository personalTariffRepository;

    @Autowired
    private PublicTariffRepository publicTariffRepository;
    @Autowired
    private PersonalCarRepository personalCarRepository;

    @Autowired
    private TransportCompensationRepository transportCompensationRepository;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private SingleTaxiTripRepository taxiTripRepository;

    @Autowired
    private LimitRepository limitRepository;

    @Value("${file.test.source-dir}")
    private String filesSourceRelativePath;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteDirectory(File directory) {
        for (var file : Optional.ofNullable(directory.listFiles()).map(Arrays::asList).orElseGet(ArrayList::new)) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }
        directory.delete();
    }

    @SneakyThrows
    @BeforeEach
    public void setUp() {
        System.setProperty("java.io.tmpdir", TARGET_TEST_FILES);
        deleteDirectory(new File(TARGET_TEST_FILES));

        testEmployee1.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_1);
        testEmployee2.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_2);
        testEmployee3.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_3);
        testEmployee4.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_4);
        testEmployee5.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_5);

        request1.setLimit(limitRepository.save(request1.getLimit()));
        request2.setLimit(limitRepository.save(request2.getLimit()));
        request3.setLimit(limitRepository.save(request3.getLimit()));
        request4.setLimit(limitRepository.save(request4.getLimit()));
        request5.setLimit(limitRepository.save(request5.getLimit()));

        request1.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_1);
        request2.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_2);
        request3.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_3);
        request4.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_4);
        request5.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_5);
        request6.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_6);

        organizationRepository.saveAndFlush(organization1);
        organizationRepository.saveAndFlush(organization2);
        organizationRepository.saveAndFlush(organization3);
        organizationRepository.saveAndFlush(organization5);

        departmentRepository.saveAndFlush(departmentHead);
        departmentRepository.saveAndFlush(departmentLocal);
        departmentRepository.saveAndFlush(department1);
        departmentRepository.saveAndFlush(department2);
        departmentRepository.saveAndFlush(department3);
        departmentRepository.saveAndFlush(department5);

        positionRepository.save(testPosition1);
        positionRepository.save(testPosition2);
        positionRepository.save(testPosition3);

        testEmployee1 = employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);
        employeeRepository.save(testEmployee4);
        employeeRepository.save(testEmployee5);

        contractorRepository.save(contractor1);
        contractorRepository.save(contractor2);
        contractorRepository.save(carsharingContractor);
        contractRepository.save(contract1);
        contractRepository.save(contract2);
        contractRepository.save(contract3);
        taxiTariffRepository.save(taxiTariff1);
        carsharingTariffRepository.save(carsharingTariff1);
        personalTariffRepository.save(personalTariff);
        publicTariffRepository.save(publicTariff);

        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        addressRepository.saveAndFlush(address3);
        addressRepository.saveAndFlush(address4);

        tripPurposeRepository.save(tripPurpose1);
        tripPurposeRepository.save(tripPurpose3);
        tripPurposeRepository.save(tripPurpose4);
        tripPurposeRepository.save(tripPurpose5);

        sharedRide1 = sharedRideRepository.save(sharedRide1);

        request4.setSharedRide(sharedRide1);

        requestRepository.save(request1);
        requestRepository.save(request2);
        requestRepository.save(request3);
        requestRepository.save(request4);
        requestRepository.save(request5);
        requestRepository.save(request6);

        waypoints1.forEach(w -> w.setRequest(request1));
        waypoints2.forEach(w -> w.setRequest(request2));
        waypoints3.forEach(w -> w.setRequest(request3));
        waypoints4.forEach(w -> w.setRequest(request4));
        waypoints5.forEach(w -> w.setRequest(request5));
        waypoints5.forEach(w -> w.setRequest(request6));

        waypointRepository.saveAll(waypoints1);
        waypointRepository.saveAll(waypoints2);
        waypointRepository.saveAll(waypoints3);
        waypointRepository.saveAll(waypoints4);
        waypointRepository.saveAll(waypoints5);

        orderKpiRepository.saveAndFlush(orderKpi1);

        employee1PersonalCar.setEmployee(testEmployee1);
        employee1PersonalCar = personalCarRepository.saveAndFlush(employee1PersonalCar);
        employee1PersonalCar.setNew(false);

        requestRepository.save(request2);

        cityTripCompensationList.forEach(tc -> tc.setRequest(request2));
        transportCompensationRepository.saveAll(cityTripCompensationList);

    }

    @AfterEach
    void tearDown() {
        new TransactionTemplate(platformTransactionManager).executeWithoutResult(status -> {
            status.setRollbackOnly();
            orderKpiRepository.deleteAllInBatch();
            waypointRepository.deleteAllInBatch();
            sharedRideRepository.deleteAllInBatch();
            transportCompensationRepository.deleteAllInBatch();
            ttrsRepository.deleteAllInBatch();
            ttrRepository.findAll().forEach(ttr -> {
                ttr.setBlankCells(null);
                ttrRepository.save(ttr);
            });
            ttrRepository.deleteAllInBatch();
            requestRepository.deleteAllInBatch();
            taxiTripRepository.deleteAllInBatch();
            personalCarRepository.deleteAllInBatch();
            employeeRepository.deleteAllInBatch();
            positionRepository.deleteAllInBatch();
            departmentRepository.deleteAllInBatch();
            organizationRepository.deleteAllInBatch();
        });
    }

    @Test
    @WithMockUser(username = USER1_ID_STR, roles = ROLE_STR)
    @DisplayName("Проверка отправки сообщения смены статуса")
    void testUpdateRequestStatus() throws Exception {
        organizationRepository.save(organization1);
        departmentRepository.save(department1);
        positionRepository.save(testPosition1);
        employeeRepository.save(testEmployee1);
        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        tripPurposeRepository.save(tripPurpose1);
        request1.setStatus(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.name());
        waypointRepository.saveAll(waypoints1);
        personalTariffRepository.save(personalTariff);
        requestRepository.saveAndFlush(request1);

        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        var start = now.minusDays(1);
        var end = now.plusDays(1);
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();
        var isCoop = request1.isCoopTrip();

        Map<String, Object> requestObject = new HashMap<>();
        var filters = RequestForPersonalReportDTO.builder()
                                                 .creationDate(dateRange)
                                                 .coopTrip(isCoop).build();
        requestObject.put("filters", Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8)));
        requestObject.put("withView", true);

        UUID organizationId = organization1.getId();
        var rawStatus = mockMvc.perform(post(String.format("/%s/xlsx/payment-report/personal", organizationId))
                                                .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")))
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(requestObject)))
                               .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

        var response = objectMapper.readValue(rawStatus, TaskResultDto.class).url().replace("/files/", "");

        var file = Path.of(TARGET_TEST_FILES, "reports", response).toFile();
        await().timeout(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).until(file::exists);

        var message = consumeMessage("service.request.status.update.from.reports", UpdateTripRequestStatusMessage.class);

        assertThat(request1.getId()).isEqualTo(message.getId());
        assertThat(TripRequestStatus.getFromString(message.getStatus())).contains(TripRequestStatus.PERSONAL_PAYMENT_AWAITING);
    }

    @Test
    @WithMockUser(username = USER2_ID_STR, roles = ROLE_STR)
    @DisplayName("Выгрузка реестра к выплате на ОТ в XLSX - Успешно")
    @Disabled("Требуется актуализация")
    void testReportForPersonalPaymentInXlsSuccess() throws Exception {
        organizationRepository.save(organization2);
        departmentRepository.save(department2);
        positionRepository.save(testPosition2);
        employeeRepository.save(testEmployee2);
        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        tripPurposeRepository.save(tripPurpose1);
        request2.setStatus(TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION.name());
        request2.setPaymentData(PaymentData.builder()
                                           .paymentPriceMain(100000L)
                                           .paymentPriceOptional(101000L)
                                           .paymentTypeCodeMain(4661)
                                           .paymentTypeCodeOptional(4666)
                                           .build());
        waypointRepository.saveAll(waypoints2);
        publicTariffRepository.save(publicTariff);
        requestRepository.save(request2);

        Map<String, Object> intervalMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        var start = now.minusDays(1);
        var end = now.plusDays(1);
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();
        var isCoop = request2.isCoopTrip();

        Map<String, Object> requestObject = new HashMap<>();
        var filters = RequestForPublicReportDTO.builder()
                                               .creationDate(dateRange)
                                               .build();
        requestObject.put("filters", Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8)));
        requestObject.put("withView", true);

        UUID organizationId = organization2.getId();
        var rawStatus = mockMvc.perform(post(String.format("/%s/xlsx/payment-report/public", organizationId))
                                                .header("Authorization", "Basic login")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(requestObject)))
                               .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

        var response = objectMapper.readValue(rawStatus, TaskResultDto.class).url().replace("/files/", "");

        var file = Path.of(TARGET_TEST_FILES, "reports", response).toFile();
        await().timeout(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).until(file::exists);

        new TransactionTemplate(platformTransactionManager).executeWithoutResult(status -> {
            status.setRollbackOnly();
            var request = requestRepository.getById(request2.getId());
            try (var inputStream = new FileInputStream(file);
                 var workbook = new XSSFWorkbook(inputStream)) {
                var sheetAt = workbook.getSheetAt(0);
                checkXlsFileForCustomPersonal(request, sheetAt);
            } catch (Exception e) {
                fail(e);
            }
        });
    }

    @Test
    @WithMockUser(username = USER2_ID_STR, roles = ROLE_STR)
    @DisplayName("Выгрузка реестра поездок на ОТ в XLSX - Успешно")
    @Disabled("Требуется переработка")
    void testReportOrganizationForPublicSuccess() throws Exception {
        organizationRepository.save(organization2);
        departmentRepository.save(department2);
        positionRepository.save(testPosition2);
        employeeRepository.save(testEmployee2);
        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        tripPurposeRepository.save(tripPurpose1);
        waypointRepository.saveAll(waypoints2);
        publicTariffRepository.save(publicTariff);
        requestRepository.save(request2);

        Map<String, Object> intervalMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        var start = now.minusDays(1);
        var end = now.plusDays(1);
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();

        Map<String, Object> requestObject = new HashMap<>();
        var filters = RequestForPublicReportDTO.builder()
                                               .creationDate(dateRange)
                                               .build();
        requestObject.put("filters", Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8)));

        UUID organizationId = organization2.getId();
        var rawStatus = mockMvc.perform(post(String.format("/%s/xlsx/trip-requests/public", organizationId))
                                                .header("Authorization", "Basic login")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(requestObject)))
                               .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

        var response = objectMapper.readValue(rawStatus, TaskResultDto.class).url().replace("/files/", "");

        var file = Path.of(TARGET_TEST_FILES, "reports", response).toFile();
        await().timeout(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).until(file::exists);

        new TransactionTemplate(platformTransactionManager).executeWithoutResult(status -> {
            try (var inputStream = new FileInputStream(file);
                 var workbook = new XSSFWorkbook(inputStream)) {
                var iterator = workbook.getSheetAt(0).getRow(1).cellIterator();
                var request = requestRepository.getById(request2.getId());
                checkXlsFileForPublic(request, iterator);
            } catch (Exception e) {
                fail(e);
            }
        });
    }

    @Test
    @WithMockUser(username = USER1_ID_STR, roles = ROLE_STR)
    @DisplayName("Выгрузка реестра поездок на ОТ в XLSX - Не найдены")
    @Disabled("Требуется актуализация")
    void testReportOrganizationForPublic() throws Exception {
        organizationRepository.save(organization2);
        departmentRepository.save(department2);
        positionRepository.save(testPosition2);
        employeeRepository.save(testEmployee2);
        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        waypointRepository.saveAll(waypoints2);
        publicTariffRepository.save(publicTariff);
        requestRepository.save(request2);

        Map<String, Object> intervalMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        var start = now.minusDays(1);
        var end = now.plusDays(1);
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();

        Map<String, Object> requestObject = new HashMap<>();
        var filters = RequestForPublicReportDTO.builder()
                                               .creationDate(dateRange)
                                               .build();
        requestObject.put("filters", Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8)));

        UUID organizationId = organization1.getId();
        var rawStatus = mockMvc.perform(post(String.format("/%s/xlsx/trip-requests/public", organizationId))
                                                .header("Authorization", "Basic login")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(requestObject)))
                               .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

        var response = objectMapper.readValue(rawStatus, TaskResultDto.class).url().replace("/files/", "");

        var file = Path.of(TARGET_TEST_FILES, "reports", response).toFile();
        await().timeout(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).until(file::exists);

        try (var inputStream = new FileInputStream(file);
             var workbook = new XSSFWorkbook(inputStream)) {
            Iterator<Cell> iterator = workbook.getSheetAt(0).getRow(1).cellIterator();
            while (iterator.hasNext()) {
                String value = iterator.next().getStringCellValue();
                assertThat(value).isEmpty();
            }

        }
    }

    @Test
    @Disabled("Почему?")
    @WithMockUser(username = USER5_ID_STR, roles = ROLE_STR)
    @DisplayName("Выгрузка реестра поездок на каршеринге в XLSX")
    void testReportForCarsharing() throws Exception {
        organizationRepository.save(organization5);
        departmentRepository.save(department5);
        contractorRepository.save(carsharingContractor);
        contractRepository.save(contract3);
        carsharingTariffRepository.save(carsharingTariff1);
        employeeRepository.save(testEmployee5);
        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        tripPurposeRepository.save(tripPurpose5);
        waypointRepository.saveAll(waypoints5);
        carsharingTariffRepository.save(carsharingTariff1);
        requestRepository.saveAndFlush(request5);

        Map<String, Object> intervalMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        var start = now.minusDays(1);
        var end = now.plusDays(1);
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();

        Map<String, Object> requestObject = new HashMap<>();
        var filters = RequestForCarsharingReportDTO.builder()
                                                   .creationDate(dateRange)
                                                   .build();
        requestObject.put("filters", Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8)));

        UUID organizationId = request5.getPassenger().getDepartment().getOrganizationId();
        var rawStatus = mockMvc.perform(post(String.format("/%s/xlsx/trip-requests/carsharing", organizationId))
                                                .header("Authorization", "Basic login")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(requestObject)))
                               .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

        var response = objectMapper.readValue(rawStatus, TaskResultDto.class).url().replace("/files/", "");

        var file = Path.of(TARGET_TEST_FILES, "reports", response).toFile();
        await().timeout(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).until(file::exists);

        try (var inputStream = new FileInputStream(file);
             var workbook = new XSSFWorkbook(inputStream)) {
            var iterator = workbook.getSheetAt(0).getRow(1).cellIterator();
            checkXlsxFileForCarsharing(request5, limit5, iterator);
        }
    }

    @Test
    @WithMockUser(username = USER2_ID_STR, roles = ROLE_STR)
    @DisplayName("Выгрузка реестра поездок на ОТ с withView параметром в XLSX")
    @Disabled("Требуется актуализация")
    void testReportForPublicWithView() throws Exception {
        organizationRepository.save(organization2);
        departmentRepository.save(department2);
        positionRepository.save(testPosition2);
        employeeRepository.save(testEmployee2);
        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        addressRepository.saveAndFlush(address3);
        tripPurposeRepository.save(tripPurpose1);
        waypointRepository.saveAll(waypoints2);
        publicTariffRepository.save(publicTariff);
        requestRepository.save(request2);

        PublicUIVisibilityDTO publicUIVisibilityDTO = PublicUIVisibilityDTO.builder()
                                                                           .requestIdVisible(true)
                                                                           .requestStatusVisible(true)
                                                                           .creationTimeVisible(true)
                                                                           .paymentPeriodVisible(true)
                                                                           .passengerDepartmentOneVisible(true)
                                                                           .passengerDepartmentTwoVisible(true)
                                                                           .passengerDepartmentThreeVisible(true)
                                                                           .passengerDepartmentFourVisible(true)
                                                                           .compensationTypeVisible(true)
                                                                           .waypointFromVisible(true)
                                                                           .waypointToVisible(true)
                                                                           .intermediateAddressVisible(true)
                                                                           .personelNumberVisible(true)
                                                                           .build();

        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        var start = now.minusDays(1);
        var end = now.plusDays(1);
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();

        Map<String, Object> requestObject = new HashMap<>();
        var filters = RequestForPublicReportDTO.builder()
                                               .creationDate(dateRange)
                                               .build();
        requestObject.put("filters", Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8)));
        requestObject.put("publicUIVisibilityDTO", publicUIVisibilityDTO);
        requestObject.put("withView", true);

        UUID organizationId = organization2.getId();
        var rawStatus = mockMvc.perform(post(String.format("/%s/xlsx/trip-requests/public", organizationId))
                                                .header("Authorization", "Basic login")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(requestObject)))
                               .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

        var response = objectMapper.readValue(rawStatus, TaskResultDto.class).url().replace("/files/", "");

        var file = Path.of(TARGET_TEST_FILES, "reports", response).toFile();
        await().timeout(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).until(file::exists);

        try (var inputStream = new FileInputStream(file);
             var workbook = new XSSFWorkbook(inputStream)) {
            var iterator = workbook.getSheetAt(0).getRow(1).cellIterator();
            checkXlsFileForCustomPublic(request2, iterator);
        }
    }

    @Test
    @WithMockUser(username = USER1_ID_STR, roles = ROLE_STR)
    @DisplayName("Выгрузка реестра поездок на ЛТ с withView параметром в XLSX")
    @Disabled("Требуется актуализация")
    void testReportForPersonalWithView() throws Exception {
        organizationRepository.save(organization1);
        departmentRepository.save(department1);
        positionRepository.save(testPosition1);
        employeeRepository.save(testEmployee1);
        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        tripPurposeRepository.save(tripPurpose1);
        personalTariffRepository.save(personalTariff);
        request1.setStatus(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.name());
        waypointRepository.saveAll(waypoints1);
        requestRepository.save(request1);

        RequestMessage requestMessage = requestMapper.toMessage(request1);
        produceMessage("service.request", requestMessage);

        PersonalUIVisibilityDTO personalUIVisibilityDTO = PersonalUIVisibilityDTO.builder()
                                                                                 .requestIdVisible(true)
                                                                                 .requestStatusVisible(true)
                                                                                 .build();

        Map<String, Object> intervalMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        var start = now.minusDays(1);
        var end = now.plusDays(1);
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();

        Map<String, Object> requestObject = new HashMap<>();
        var filters = RequestForPersonalReportDTO.builder()
                                                 .creationDate(dateRange)
                                                 .organizationId(UUID.randomUUID())
                                                 .personalUIVisibilityDTO(personalUIVisibilityDTO)
                                                 .build();
        requestObject.put("filters", Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8)));
        requestObject.put("withView", true);

        UUID organizationId = organization1.getId();
        var rawStatus = mockMvc.perform(post(String.format("/%s/xlsx/payment-report/personal", organizationId))
                                                .header("Authorization", "Basic login")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(requestObject)))
                               .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

        var response = objectMapper.readValue(rawStatus, TaskResultDto.class).url().replace("/files/", "");

        var file = Path.of(TARGET_TEST_FILES, "reports", response).toFile();
        await().timeout(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).until(file::exists);

        new TransactionTemplate(platformTransactionManager).executeWithoutResult(status -> {
            status.setRollbackOnly();
            try (var inputStream = new FileInputStream(file);
                 var workbook = new XSSFWorkbook(inputStream)) {
                Request request = requestRepository.getById(request1.getId());
                XSSFSheet sheetAt = workbook.getSheetAt(0);
                checkXlsFileForCustomPersonal(request, sheetAt);
            } catch (IOException e) {
                fail(e);
            }
        });
    }

    @Test
    @WithMockUser(username = USER1_ID_STR, roles = ROLE_STR)
    @DisplayName("Выгрузка реестра поездок на ЛТ в XLSX")
    @Disabled("Требуется актуализация")
    void testReportForPersonalWithAggregationPage() throws Exception {
        var now = LocalDateTime.now(ZoneOffset.UTC);

        organizationRepository.saveAndFlush(organization1);
        departmentRepository.save(department1);
        positionRepository.save(testPosition1);

        testEmployee1.setUserId(UUID.fromString(USER1_ID_STR));
        employeeRepository.saveAndFlush(testEmployee1);

        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        tripPurposeRepository.save(tripPurpose1);
        personalTariffRepository.save(personalTariff);

        request1.setCreationTime(now);
        request1.setStatus(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.name());
        waypointRepository.saveAll(waypoints1);
        requestRepository.save(request1);

        RequestMessage requestMessage = requestMapper.toMessage(request1);
        produceMessage("service.request", requestMessage);

        var start = now.minusDays(1);
        var end = now.plusDays(1);
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();

        Map<String, Object> requestObject = new HashMap<>();
        var filters = RequestForPersonalReportDTO.builder()
                                                 .creationDate(dateRange)
                                                 .organizationId(UUID.randomUUID())
                                                 .build();
        requestObject.put("filters", Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8)));
        requestObject.put("withView", true);

        UUID organizationId = organization1.getId();
        var rawStatus = mockMvc.perform(post(String.format("/%s/xlsx/payment-report/personal", organizationId))
                                                .header("Authorization", "Basic login")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(requestObject)))
                               .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

        var response = objectMapper.readValue(rawStatus, TaskResultDto.class).url().replace("/files/", "");

        var file = Path.of(TARGET_TEST_FILES, "reports", response).toFile();
        await().timeout(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).until(file::exists);

        new TransactionTemplate(platformTransactionManager).executeWithoutResult(status -> {
            status.setRollbackOnly();
            var request = requestRepository.getById(request1.getId());
            try (var fis = new FileInputStream(file);
                 var workbook = new XSSFWorkbook(fis)) {
                var sheetAt = workbook.getSheetAt(1);
                checkXlsFileForCustomPersonal(request, sheetAt);
            } catch (IOException e) {
                fail(e);
            }
        });
    }

    @Test
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    @DisplayName("Выгрузка реестра поездок для такси single trip")
    @Disabled("Требуется переработка")
    void testTaxiReports1() throws Exception {
        organizationRepository.save(organization3);
        departmentRepository.save(department3);
        contractorRepository.save(contractor1);
        contractRepository.save(contract1);
        taxiTariffRepository.save(taxiTariff1);
        positionRepository.save(testPosition3);
        employeeRepository.save(testEmployee3);
        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        addressRepository.saveAndFlush(address3);
        tripPurposeRepository.save(tripPurpose3);
        waypointRepository.saveAll(waypoints3);
        taxiTariffRepository.save(taxiTariff1);
        requestRepository.saveAndFlush(request3);
        singleRepository.save(singleTaxiTrip1);
        departmentService.setDepartmentHierarchy(Map.of(request3.getId(), request3));

        importExcelRegistry(taxiTripRegistryDTO1, FILE_NAME2, filesSourceRelativePath, mockMvc, objectMapper);

        var checkDTO = mapper.toCheckRegistry(ttrsRepository.findByTaxiId(singleTaxiTrip1.getTaxiId()).get());

        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        var start = now.minusDays(1);
        var end = now.plusDays(1);
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();

        Map<String, Object> requestObject = new HashMap<>();
        var filters = RequestForPersonalReportDTO.builder()
                                                 .creationDate(dateRange)
                                                 .organizationId(UUID.randomUUID())
                                                 .build();
        requestObject.put("filters", Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8)));
        var rawStatus = mockMvc.perform(post(String.format("/%s/xlsx/trip-requests/taxi", organization3.getId()))
                                                .header("Authorization", "Basic login")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(requestObject)))
                               .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

        var response = objectMapper.readValue(rawStatus, TaskResultDto.class).url().replace("/files/", "");

        var file = Path.of(TARGET_TEST_FILES, "reports", response).toFile();
        await().timeout(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).until(file::exists);

        new TransactionTemplate(platformTransactionManager).executeWithoutResult(status -> {
            try (var inputStream = new FileInputStream(file);
                 var workbook = new XSSFWorkbook(inputStream)) {
                var iterator = workbook.getSheetAt(0).getRow(1).cellIterator();
                status.setRollbackOnly();
                var request = requestRepository.getById(request3.getId());
                var singleTaxiTrip = taxiTripRepository.getById(singleTaxiTrip1.getId());
                var taxiTariff = taxiTariffRepository.getById(taxiTariff1.getId());
                request.setDepartmentHierarchy(departmentService.getDepartmentHierarchy(request.getPassenger().getDepartment()));
                checkXlsFileForTaxi(request, singleTaxiTrip, null, taxiTariff, limit3, checkDTO, iterator);
            } catch (Exception e) {
                fail(e);
            }
        });
    }

    @Test
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    @DisplayName("Выгрузка реестра поездок для такси coop trip")
    @Disabled("Требуется переработка")
    void testTaxiReports2() throws Exception {
        contractorRepository.save(contractor2);
        contractRepository.save(contract2);
        employeeRepository.save(testEmployee4);
        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        addressRepository.saveAndFlush(address3);
        addressRepository.saveAndFlush(address4);
        tripPurposeRepository.save(tripPurpose4);
        sharedRideKPI1 = sharedRequestKpiRepository.save(sharedRideKPI1);
        orderKpi1.setKpiId(sharedRideKPI1.getId());
        orderKpi1 = orderKpiRepository.save(orderKpi1);
        sharedRideKPI1.getOrdersKpi().add(orderKpi1);
        sharedRideKPI1 = sharedRequestKpiRepository.save(sharedRideKPI1);
        sharedRide1.setKpi(sharedRideKPI1);
        sharedRideRepository.save(sharedRide1);
        request4.setSharedRide(sharedRide1);
        waypointRepository.saveAll(waypoints4);
        requestRepository.saveAndFlush(request4);
        coopRepository.save(coopTaxiTrip1);
        departmentService.setDepartmentHierarchy(Map.of(request4.getId(), request4));

        importExcelRegistry(taxiTripRegistryDTO2, FILE_NAME2, filesSourceRelativePath, mockMvc, objectMapper);

        TaxiTripRegistryCheckDTO checkDTO =
                mapper.toCheckRegistry(ttrsRepository.findByTaxiId(coopTaxiTrip1.getTaxiId()).get());

        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        var start = now.minusDays(1);
        var end = now.plusDays(1);
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();

        Map<String, Object> requestObject = new HashMap<>();
        var filters = RequestForPersonalReportDTO.builder()
                                                 .creationDate(dateRange)
                                                 .coopTrip(true)
                                                 .build();
        requestObject.put("filters", Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8)));
        var rawStatus = mockMvc.perform(post(String.format("/%s/xlsx/trip-requests/taxi", organization3.getId()))
                                                .header("Authorization", "Basic login")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(requestObject)))
                               .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

        var response = objectMapper.readValue(rawStatus, TaskResultDto.class).url().replace("/files/", "");

        var file = Path.of(TARGET_TEST_FILES, "reports", response).toFile();
        await().timeout(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).until(file::exists);

        new TransactionTemplate(platformTransactionManager).executeWithoutResult(status -> {
            try (var inputStream = new FileInputStream(file);
                 var workbook = new XSSFWorkbook(inputStream)) {
                var iterator = workbook.getSheetAt(0).getRow(1).cellIterator();
                var request = requestRepository.getById(request4.getId());
                request.setDepartmentHierarchy(departmentService.getDepartmentHierarchy(request.getPassenger().getDepartment()));
                var coopTaxiTrip = coopRepository.getById(coopTaxiTrip1.getId());
                var taxiTariff = taxiTariffRepository.getById(taxiTariff1.getId());
                checkXlsFileForTaxi(request, null, coopTaxiTrip, taxiTariff, limit4, checkDTO, iterator);
            } catch (Exception e) {
                fail(e);
            }
        });
    }

    @Test
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    @DisplayName("Выгрузка реестра поездок для такси coop trip без заполнения всех полей")
    @Disabled("Требуется актуализация")
    void testTaxiPartReports() throws Exception {
        contractorRepository.save(contractor2);
        contractRepository.save(contract2);
        employeeRepository.save(testEmployee4);
        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        addressRepository.saveAndFlush(address3);
        addressRepository.saveAndFlush(address4);
        tripPurposeRepository.save(tripPurpose4);
       /* sharedRideKPI1 = sharedRequestKpiRepository.save(sharedRideKPI1);
        orderKpi1.setKpi(sharedRideKPI1);
        orderKpi1 = orderKpiRepository.save(orderKpi1);
        sharedRideKPI1.getOrdersKpi().add(orderKpi1);
        sharedRideKPI1 = sharedRequestKpiRepository.save(sharedRideKPI1);
        magentaSharedRequest1.setKpi(sharedRideKPI1);*/
        sharedRideRepository.save(sharedRide1);
        request4.setSharedRide(sharedRide1);
        waypointRepository.saveAll(waypoints4);
        requestRepository.saveAndFlush(request4);
        departmentService.setDepartmentHierarchy(Map.of(request4.getId(), request4));
        //coopRepository.save(coopTaxiTrip1);

        importExcelRegistry(taxiTripRegistryDTO2, FILE_NAME2, filesSourceRelativePath, mockMvc,
                            objectMapper);

        TaxiTripRegistryCheckDTO checkDTO =
                mapper.toCheckRegistry(ttrsRepository.findByTaxiId(coopTaxiTrip1.getTaxiId()).get());

        Map<String, Object> intervalMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        var start = now.minusDays(1);
        var end = now.plusDays(1);
        var dateRange = RequestReportDTO.DateRange.builder().start(start).end(end).build();

        Map<String, Object> requestObject = new HashMap<>();
        var filters = RequestForPersonalReportDTO.builder()
                                                 .creationDate(dateRange)
                                                 .coopTrip(true)
                                                 .build();
        requestObject.put("filters", Base64.getEncoder().encode(objectMapper.writeValueAsString(filters).getBytes(StandardCharsets.UTF_8)));
        var rawStatus = mockMvc.perform(post(String.format("/%s/xlsx/trip-requests/taxi", organization3.getId()))
                                                .header("Authorization", "Basic login")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(requestObject)))
                               .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

        var response = objectMapper.readValue(rawStatus, TaskResultDto.class).url().replace("/files/", "");

        var file = Path.of(TARGET_TEST_FILES, "reports", response).toFile();
        await().timeout(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).until(file::exists);

        new TransactionTemplate(platformTransactionManager).executeWithoutResult(status -> {
            status.setRollbackOnly();
            try (var inputStream = new FileInputStream(file);
                 var workbook = new XSSFWorkbook(inputStream)) {
                var request = requestRepository.getById(request4.getId());
                String unavailable = "Н/Д";
                Iterator<Cell> iterator = workbook.getSheetAt(0).getRow(1).cellIterator();
                while (iterator.hasNext()) {
                    Cell cell = iterator.next();
                    switch (cell.getColumnIndex()) {
                        case 0 -> assertThat(cell.getStringCellValue()).isEqualTo("Такси");
                        case 1 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getHumanReadableId());
                        case 2 -> assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(request.getSharedRide().getId()));
                        case 21, 28, 29, 30, 31, 38, 46, 50, 52 -> assertThat(cell.getStringCellValue()).isEqualTo(unavailable);
                        case 32 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getPurpose().getPurpose());
                        case 39 -> assertThat(cell.getStringCellValue()).isEqualTo("Not implemented yet");
                    }
                }
            } catch (Exception e) {
                fail(e);
            }
        });
    }

    @Test
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    @DisplayName("Выгрузка реестра поездок с пустым фильтром ")
    @Disabled("Требуется переработка")
    void testTaxiReports3() throws Exception {
        contractorRepository.save(contractor2);
        contractRepository.save(contract2);
        taxiTariffRepository.save(taxiTariff1);
        employeeRepository.save(testEmployee4);
        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        addressRepository.saveAndFlush(address3);
        addressRepository.saveAndFlush(address4);
        tripPurposeRepository.save(tripPurpose4);
        sharedRideKPI1 = sharedRequestKpiRepository.save(sharedRideKPI1);
        orderKpi1.setKpiId(sharedRideKPI1.getId());
        orderKpi1 = orderKpiRepository.save(orderKpi1);
        sharedRideKPI1.getOrdersKpi().add(orderKpi1);
        sharedRideKPI1 = sharedRequestKpiRepository.save(sharedRideKPI1);
        sharedRide1.setKpi(sharedRideKPI1);
        sharedRideRepository.save(sharedRide1);
        request4.setSharedRide(sharedRide1);
        waypointRepository.saveAll(waypoints4);
        taxiTariffRepository.save(taxiTariff1);
        requestRepository.saveAndFlush(request4);
        coopRepository.save(coopTaxiTrip1);
        taxiTripRepository.save(singleTaxiTrip1);
        departmentService.setDepartmentHierarchy(Map.of(request4.getId(), request4));

        importExcelRegistry(taxiTripRegistryDTO2, FILE_NAME2, filesSourceRelativePath, mockMvc,
                            objectMapper);

        var checkDTO = mapper.toCheckRegistry(ttrsRepository.findByTaxiId(coopTaxiTrip1.getTaxiId()).get());

        var rawStatus = mockMvc.perform(post(String.format("/%s/xlsx/trip-requests/taxi", organization3.getId()))
                                                .header("Authorization", "Basic login")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(new HashMap<>())))
                               .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

        var response = objectMapper.readValue(rawStatus, TaskResultDto.class).url().replace("/files/", "");

        var file = Path.of(TARGET_TEST_FILES, "reports", response).toFile();
        await().timeout(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).until(file::exists);

        new TransactionTemplate(platformTransactionManager).executeWithoutResult(status -> {
            status.setRollbackOnly();
            try (var inputStream = new FileInputStream(file);
                 var workbook = new XSSFWorkbook(inputStream)) {
                var iterator = workbook.getSheetAt(0).getRow(2).cellIterator();
                var request = requestRepository.getById(request4.getId());
                request.setDepartmentHierarchy(departmentService.getDepartmentHierarchy(request.getPassenger().getDepartment()));
                var coopTaxiTrip = coopRepository.getById(coopTaxiTrip1.getId());
                var taxiTariff = taxiTariffRepository.getById(taxiTariff1.getId());
                checkXlsFileForTaxi(request, null, coopTaxiTrip, taxiTariff, request.getLimit(), checkDTO, iterator);
            } catch (Exception e) {
                fail(e);
            }
        });
    }

    @Test
    @WithMockUser(username = USER1_ID_STR, roles = ROLE_STR)
    @DisplayName("Выгрузка реестра поездок личного транспорта")
    @Disabled("Требуется актуализация")
    void testTaxiReportsForPersonal() throws Exception {

        organizationRepository.save(organization1);
        departmentRepository.save(department1);
        positionRepository.save(testPosition1);
        employeeRepository.save(testEmployee1);
        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        tripPurposeRepository.save(tripPurpose1);
        personalTariffRepository.save(personalTariff);
        request1.setStatus(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.name());

        var address = Address.builder()
                             .id(UUID.randomUUID())
                             .building("IntermediateBuilding")
                             .city("IntermediateCity")
                             .country("IntermediateCountry")
                             .house("IntermediateHouse")
                             .region("IntermediateRegion")
                             .street("IntermediateStreet")
                             .structure("IntermediateStructure")
                             .build();

        addressRepository.saveAndFlush(address);

        var waypoint = Waypoint.builder()
                               .id(UUID.randomUUID())
                               .address(address)
                               .waitTime(Duration.ofHours(1))
                               .build();

        waypoints1.add(waypoint);
        waypointRepository.saveAll(waypoints1);
        request1.getWaypoints().add(waypoint);
        requestRepository.save(request1);

        var requestMessage = requestMapper.toMessage(request1);
        produceMessage("service.request", requestMessage);

        PersonalUIVisibilityDTO personalUIVisibilityDTO = PersonalUIVisibilityDTO.builder()
                                                                                 .paymentPeriodVisible(true)
                                                                                 .desiredDateVisible(true)
                                                                                 .build();

        Map<String, Object> requestObject = new HashMap<>();
        requestObject.put("personalUIVisibilityDTO", personalUIVisibilityDTO);
        requestObject.put("withView", true);

        UUID organizationId = organization1.getId();
        var rawStatus = mockMvc.perform(post(String.format("/%s/xlsx/trip-requests/personal", organizationId))
                                                .header("Authorization", "Basic login")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(requestObject)))
                               .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

        var response = objectMapper.readValue(rawStatus, TaskResultDto.class).url().replace("/files/", "");

        var file = Path.of(TARGET_TEST_FILES, "reports", response).toFile();
        await().timeout(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).until(file::exists);

        try (var inputStream = new FileInputStream(file);
             var workbook = new XSSFWorkbook(inputStream)) {
            new TransactionTemplate(platformTransactionManager).executeWithoutResult(status -> {
                status.setRollbackOnly();
                Request request = requestRepository.getById(request1.getId());

                Iterator<Cell> iterator = workbook.getSheetAt(0).getRow(1).cellIterator();

                String unavailable = "Н/Д";

                while (iterator.hasNext()) {
                    Cell cell = iterator.next();
                    switch (cell.getColumnIndex()) {
                        case 0 -> assertThat(cell.getStringCellValue()).isEqualTo(
                                request.getCreationTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
                        case 1 -> assertThat(cell.getStringCellValue()).isEqualTo(
                                request.getDesiredDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                        case 2 -> assertThat(cell.getStringCellValue()).isEqualTo(
                                request.getDesiredDate().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                        case 3 -> assertThat(cell.getStringCellValue()).isEqualTo(
                                request.getWaypoints().get(1).getAddress().getRegion() + ", " +
                                request.getWaypoints().get(1).getAddress().getStreet() +
                                ", " + request.getWaypoints().get(1).getAddress().getHouse());
                        case 4 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getPeriodOfPayment() + "");
                        case 5 -> assertThat(cell.getStringCellValue()).isEqualTo(unavailable);
                        case 6 -> assertThat(cell.getStringCellValue()).isEqualTo(
                                request.getWaypoints().get(0).getAddress().getRegion() + ", " +
                                request.getWaypoints().get(0).getAddress().getStreet() +
                                ", " + request.getWaypoints().get(0).getAddress().getHouse());
                        case 7 -> assertThat(cell.getStringCellValue()).isEqualTo(
                                request.getWaypoints().get(2).getAddress().getRegion() + ", " +
                                request.getWaypoints().get(2).getAddress().getStreet() +
                                ", " + request.getWaypoints().get(2).getAddress().getHouse());
                    }
                }
            });
        }
    }
}
